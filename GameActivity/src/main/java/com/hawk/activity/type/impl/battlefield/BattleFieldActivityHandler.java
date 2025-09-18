package com.hawk.activity.type.impl.battlefield;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldActivityKVCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldCellAwardCfg;
import com.hawk.activity.type.impl.battlefield.entity.BattleFieldEntity;
import com.hawk.game.protocol.Activity.BattleFieldDiceBuyReq;
import com.hawk.game.protocol.Activity.BattleFieldDiceOneRollReq;
import com.hawk.game.protocol.Activity.BattleFieldDiceOneRollResp;
import com.hawk.game.protocol.Activity.BattleFieldDiceRollReq;
import com.hawk.game.protocol.Activity.BattleFieldLoginRewardReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 战场寻宝活动
 * 
 * @author lating
 *
 */
public class BattleFieldActivityHandler extends ActivityProtocolHandler {

	/**
	 * 投掷骰子
	 * 
	 * @param protocol
	 * @param playerId
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BATTLE_FIELD_ROLL_DICE_REQ_VALUE)
	public boolean onRoll(HawkProtocol protocol, String playerId) {
		BattleFieldActivity activity = getActivity(ActivityType.BATTLE_FIELD_ACTIVITY);
		BattleFieldDiceRollReq req = protocol.parseProtocol(BattleFieldDiceRollReq.getDefaultInstance());
		boolean useGold = req.hasUseGold() ? req.getUseGold() : false;
		int code = activity.onRollDice(playerId, req.getType(), req.getPoint(), useGold, protocol.getType());
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}

		responseSuccess(playerId, protocol.getType());
		return true;
	}

	/**
	 * 领取登录奖励
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BATTLE_FIELD_LOGIN_REWARD_REQ_VALUE)
	public boolean onLoginReward(HawkProtocol protocol, String playerId) {
		BattleFieldActivity activity = getActivity(ActivityType.BATTLE_FIELD_ACTIVITY);
		BattleFieldLoginRewardReq req = protocol.parseProtocol(BattleFieldLoginRewardReq.getDefaultInstance());
		int code = activity.onReceiveAccLoginAward(playerId, req.getDay(), protocol.getType());
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}

		responseSuccess(playerId, protocol.getType());
		return true;
	}

	/**
	 * 请求活动页面信息
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BATTLE_FIELD_PAGE_INFO_REQ_VALUE)
	public boolean onShare(HawkProtocol protocol, String playerId) {
		BattleFieldActivity activity = getActivity(ActivityType.BATTLE_FIELD_ACTIVITY);
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return false;
		}

		activity.syncCellInfo(playerId);
		responseSuccess(playerId, protocol.getType());
		return true;
	}

	@ProtocolHandler(code = HP.code.BATTLE_FIELD_DICE_BUY_REQ_VALUE)
	public boolean onBuy(HawkProtocol protocol, String playerId) {
		BattleFieldActivity activity = getActivity(ActivityType.BATTLE_FIELD_ACTIVITY);
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return false;
		}
		BattleFieldDiceBuyReq req = protocol.parseProtocol(BattleFieldDiceBuyReq.getDefaultInstance());

		int code = activity.buy(playerId, req.getOType(), req.getONum());
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}

	@ProtocolHandler(code = HP.code.BATTLE_FIELD_YIJIANPAOTU_REQ_VALUE)
	public boolean onYijianpaotu(HawkProtocol protocol, String playerId) {
		
		BattleFieldDiceOneRollReq req = protocol.parseProtocol(BattleFieldDiceOneRollReq.getDefaultInstance());
		
		BattleFieldActivity activity = getActivity(ActivityType.BATTLE_FIELD_ACTIVITY);
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return false;
		}
		Optional<BattleFieldEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		BattleFieldActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		BattleFieldEntity entity = opEntity.get();
		if(entity.getYijianpaotu() + req.getCount() > kvcfg.getOneTimeLimit()){
			HawkLog.errPrintln("onYijianpaotu over limit playerId", playerId);
			return true;
		}

		List<RewardItem.Builder> dicePrice = RewardHelper.toRewardItemList(kvcfg.getOneConsume());
		dicePrice.forEach(item -> item.setItemCount(item.getItemCount() * req.getCount()));
		
		List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
		for (RewardItem.Builder cost : dicePrice) {
			int itemId = cost.getItemId();
			// 消耗
			int diceCount = activity.getDataGeter().getItemNum(playerId, itemId);
			if (diceCount < cost.getItemCount()) {
				String award = itemId == kvcfg.getFixedDiceItemId() ? kvcfg.getControlDicePrice() : kvcfg.getOrdinaryDicePrice();
				List<RewardItem.Builder> buyPrice = RewardHelper.toRewardItemList(award);
				buyPrice.forEach(build -> build.setItemCount(build.getItemCount() * (cost.getItemCount() - diceCount)));
				consumeItemList.addAll(buyPrice);
			}
			cost.setItemCount(Math.min(diceCount, cost.getItemCount()));
			consumeItemList.add(cost);
			
		}
		boolean success = activity.getDataGeter().consumeItems(playerId, consumeItemList, HP.code.BATTLE_FIELD_YIJIANPAOTU_REQ_VALUE, Action.BATTLE_FIELD_YIJIANPAOTU_CONSUME);
		if (!success) {
			return true;
		}
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		rewardList.addAll(RewardHelper.toRewardItemList(kvcfg.getFinalAward()));
		ConfigIterator<BattleFieldCellAwardCfg> poolIt = HawkConfigManager.getInstance().getConfigIterator(BattleFieldCellAwardCfg.class);
		for (BattleFieldCellAwardCfg cfg : poolIt) {
			rewardList.addAll(RewardHelper.toRewardItemList(cfg.getRewards()));
		}
		rewardList.forEach(item -> item.setItemCount(item.getItemCount() * req.getCount()));
		
		rewardList = RewardHelper.mergeRewardItem(rewardList);
		ActivityReward reward = new ActivityReward(rewardList, Action.BATTLE_FIELD_ROLLDICE_AWARD);
		reward.setAlert(false);
		activity.postReward(playerId, reward, true);
		entity.setYijianpaotu(entity.getYijianpaotu() + req.getCount());
		BattleFieldDiceOneRollResp.Builder resp = BattleFieldDiceOneRollResp.newBuilder();
		rewardList.forEach(e -> resp.addRewards(e));
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BATTLE_FIELD_YIJIANPAOTU_RESP, resp));
		activity.syncCellInfo(playerId);
		return true;
	}
}
