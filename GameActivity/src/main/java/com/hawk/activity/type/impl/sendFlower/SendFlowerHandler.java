package com.hawk.activity.type.impl.sendFlower;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.google.common.base.Objects;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.event.impl.SendFlowerEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerActivityKVCfg;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerCountCfg;
import com.hawk.activity.type.impl.sendFlower.entity.SendFlowerEntity;
import com.hawk.game.protocol.Activity.PBSendFlowerBuyReq;
import com.hawk.game.protocol.Activity.PBSendFlowerResp;
import com.hawk.game.protocol.Activity.PBSendFlowerSendReq;
import com.hawk.game.protocol.Activity.PBSendFlowerType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/** 机甲觉醒
 * 
 * @author Jesse */
public class SendFlowerHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.SEND_FLOWER_SEND_C_VALUE)
	public boolean onSongHua(HawkProtocol protocol, String playerId) {
		PBSendFlowerSendReq req = protocol.parseProtocol(PBSendFlowerSendReq.getDefaultInstance());
		if (Objects.equal(req.getToPlayerId(), playerId)) {
			return false;
		}
		
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		if (dataGeter.isCrossPlayer(req.getToPlayerId())) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
			return false;
		}

		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		// 扣费
		SendFlowerActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SendFlowerActivityKVCfg.class);
		final int Flower_Id = kvCfg.getSendItem();
		Reward.RewardItem.Builder cost = RewardHelper.toRewardItem(ItemType.TOOL_VALUE, Flower_Id, req.getCount());
		// 拥有道具数
		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, Arrays.asList(cost), protocol.getType(), Action.SEND_FLOWER_SONG);
		if (consumeResult == false) {
			return false;
		}

		ActivityManager.getInstance().postEvent(new SendFlowerEvent(playerId, req.getToPlayerId(), req.getCount()));
		responseSuccess(playerId, protocol.getType());
		HawkLog.logPrintln("SendFlowerActivity send playerId: {},toId:{} num:{}", playerId, req.getToPlayerId(), req.getCount());
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_BUY_C_VALUE)
	public boolean onMaiHua(HawkProtocol protocol, String playerId) {
		PBSendFlowerBuyReq req = protocol.parseProtocol(PBSendFlowerBuyReq.getDefaultInstance());
		SendFlowerCountCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SendFlowerCountCfg.class, req.getCfgId());

		// 扣费
		List<Reward.RewardItem.Builder> cost = RewardHelper.toRewardItemList(cfg.getMoney());
		// 拥有道具数
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, cost, protocol.getType(), Action.SEND_FLOWER_MAI);
		if (consumeResult == false) {
			return false;
		}

		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getCount());
		ActivityReward reward = new ActivityReward(rewardList, Action.SEND_FLOWER_MAI);
		reward.setAlert(true);
		reward.setOrginType(RewardOrginType.SHOPPING_ITEM, activity.getActivityId());
		activity.postReward(playerId, reward);
		responseSuccess(playerId, protocol.getType());
		return true;
	}

	/** 送花排行榜单信息 */
	@ProtocolHandler(code = HP.code.SEND_FLOWER_SEND_RANK_C_VALUE)
	public boolean onSongHuaRankList(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		activity.pullRankInfo(playerId, PBSendFlowerType.SONG_HUA_TYPE);
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_SHOU_RANK_C_VALUE)
	public boolean onShouHuaRankList(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		activity.pullRankInfo(playerId, PBSendFlowerType.SHOU_HUA_TYPE);
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_SEND_RECORD_C_VALUE)
	public boolean onSongHuaRecordList(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		activity.pullRecordInfo(playerId, PBSendFlowerType.SONG_HUA_TYPE);
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_SHOU_RECORD_C_VALUE)
	public boolean onShouHuaRecordList(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		activity.pullRecordInfo(playerId, PBSendFlowerType.SHOU_HUA_TYPE);
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_SYNC_C_VALUE)
	public boolean onShouHuaSync(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		Optional<SendFlowerEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		SendFlowerEntity entity = opEntity.get();
		PBSendFlowerResp.Builder resp = PBSendFlowerResp.newBuilder()
				.setSendNum(entity.getSongHua())
				.setShouNum(entity.getShouHua())
				.setLaPiaoShiJian(entity.getLaPiao());

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SEND_FLOWER_SYNC_S, resp));
		return true;
	}

	@ProtocolHandler(code = HP.code.SEND_FLOWER_HELP_REQ_VALUE)
	public boolean onQueryHelp(HawkProtocol protocol, String playerId) {
		SendFlowerActivity activity = getActivity(ActivityType.SEND_FLOWER_HUA);
		Optional<SendFlowerEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		SendFlowerEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		if (entity.getLaPiao() > now) {
			return false;
		}
		SendFlowerActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SendFlowerActivityKVCfg.class);
		entity.setLaPiao(now + kvCfg.getLapiaoCd() * 1000);

		activity.addWorldBroadcastMsg(ChatType.CHAT_ALLIANCE, NoticeCfgId.SEND_FLOWER_QIGAI, playerId, playerId);
		responseSuccess(playerId, protocol.getType());
		return true;
	}

}
