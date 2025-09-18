package com.hawk.activity.type.impl.treasureCavalry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryActivityKVCfg;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryRewardCfg;
import com.hawk.activity.type.impl.treasureCavalry.entity.TreasureCavalryEntity;
import com.hawk.game.protocol.Activity.PBTreasuryOpen;
import com.hawk.game.protocol.Activity.PBTreasuryRefreshResp;
import com.hawk.game.protocol.Activity.PBTreasuryRefreshResp.Builder;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 机甲觉醒
 * 
 * @author Jesse
 */
public class TreasureCavalryHandler extends ActivityProtocolHandler {

	/**
	 * 同步
	 */
	@ProtocolHandler(code = HP.code.TREASURE_CAVALRY_INFO_C_VALUE)
	public boolean onReqInfo(HawkProtocol protocol, String playerId) {
		TreasureCavalryActivity activity = getActivity(ActivityType.TREASURE_CAVALRY);
		activity.sync(playerId);
		return true;
	}

	/**
	 * 翻
	 */
	@ProtocolHandler(code = HP.code.TREASURE_CAVALRY_OPEN_VALUE)
	public boolean onOpen(HawkProtocol protocol, String playerId) {
		PBTreasuryOpen req = protocol.parseProtocol(PBTreasuryOpen.getDefaultInstance());
		int index = req.getIndex();
		if (index < 0 || index > 8) {
			return false;
		}
		TreasureCavalryActivity activity = getActivity(ActivityType.TREASURE_CAVALRY);
		Optional<TreasureCavalryEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		activity.sync(playerId);
		TreasureCavalryEntity entity = opEntity.get();
		List<Integer> itemList = entity.getItemList();
		if (itemList.get(index) > 0) {
			return false;
		}

		TreasureCavalryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TreasureCavalryActivityKVCfg.class);
		int openCount = 0; // 已翻
		int multipleCount = 0; // 已翻倍数道具数
		for (Integer i : itemList) {
			if (i.intValue() == 0) {
				continue;
			}
			openCount++;
			TreasureCavalryRewardCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(TreasureCavalryRewardCfg.class, i);
			Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(rcfg.getReward());
			if (kvCfg.isMultipleItem(reward.getItemId())) {
				multipleCount++;
			}
		}
		if (openCount == 0 && entity.getMultiple() > 0) {
			entity.setMultiple(0);
		}
		boolean is4_0 = openCount == 4 && multipleCount == 0;
		boolean is8_1 = openCount == 8 && multipleCount == 1;

		ConfigIterator<TreasureCavalryRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(TreasureCavalryRewardCfg.class);
		// 剩余可抽宝藏
		List<TreasureCavalryRewardCfg> rlist = new ArrayList<>();
		List<TreasureCavalryRewardCfg> allLeft = new ArrayList<>();
		for (TreasureCavalryRewardCfg cfg : it) {
			if(cfg.getPool() != entity.getPool()){
				continue;
			}
			if (itemList.contains(cfg.getId())) {
				continue;
			}
			allLeft.add(cfg);
			Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getReward());
			if (is4_0 || is8_1) {
				if (!kvCfg.isMultipleItem(reward.getItemId())) {
					continue;
				}
			}
			if (entity.getMultiple() > 0) {
				if (kvCfg.isMultipleItem(reward.getItemId())) {
					continue;
				}
			}
			rlist.add(cfg);
		}
		
		if(rlist.isEmpty()){
			rlist.addAll(allLeft);
		}

		TreasureCavalryRewardCfg cfg = HawkRand.randomWeightObject(rlist);

		// 扣费
		Reward.RewardItem.Builder openCost = RewardHelper.toRewardItem(kvCfg.getTreasureCost(openCount));
		final int extryRewardCount = (int) openCost.getItemCount();
		// 拥有道具数
		int ticketCount = activity.getDataGeter().getItemNum(playerId, openCost.getItemId());
		List<Reward.RewardItem.Builder> cost = new ArrayList<>();
		if (ticketCount < openCost.getItemCount()) {
			long left = openCost.getItemCount() - ticketCount;
			Reward.RewardItem.Builder itemOnecePrice = RewardHelper.toRewardItem(kvCfg.getItemOnecePrice());
			itemOnecePrice.setItemCount(itemOnecePrice.getItemCount() * left);
			openCost.setItemCount(ticketCount);

			cost.add(openCost);
			cost.add(itemOnecePrice);
		} else {
			cost.add(openCost);
		}

		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, cost, protocol.getType(), Action.TREASURE_CALVALRY_OPEN);
		if (consumeResult == false) {
			return false;
		}

		itemList.set(index, cfg.getId());

//		// 必得奖励
		if (StringUtils.isNotEmpty(kvCfg.getExtReward())) {
			List<RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExtReward());
			result.forEach(ite -> ite.setItemCount(ite.getItemCount() * extryRewardCount));
			ActivityReward reward = new ActivityReward(result, Action.TREASURE_CALVALRY_OPEN);
			reward.setAlert(false);
			reward.setOrginType(RewardOrginType.TREA_CAVA_REWARD, activity.getActivityId());
			activity.postReward(playerId, reward);
		}
		Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getReward());
		if (entity.getMultiple() > 0) {
			reward.setItemCount(reward.getItemCount() * entity.getMultiple());
			entity.setMultiple(0);
		}
		entity.notifyUpdate();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (!kvCfg.isMultipleItem(reward.getItemId())) {
			rewardList.add(reward);
		}else{
			entity.setMultiple((int) reward.getItemCount());
		}
		ActivityReward rewards = new ActivityReward(rewardList, Action.TREASURE_CALVALRY_OPEN);
		rewards.setAlert(true);
		rewards.setOrginType(RewardOrginType.TREA_CAVA_REWARD, activity.getActivityId());
		activity.postReward(playerId, rewards);
		activity.sync(playerId);
		responseSuccess(playerId, protocol.getType());
		
		logger.info("TREASURE_CALVALRY_OPEN playerId={} index={} record={} reward={}", playerId, index, rewardList, RewardHelper.toItemString(reward.build()));
		return true;
	}

	/**
	 * 刷新
	 */
	@ProtocolHandler(code = HP.code.TREASURE_CAVALRY_REFRESH_C_VALUE)
	public boolean onRefresh(HawkProtocol protocol, String playerId) {
		TreasureCavalryActivity activity = getActivity(ActivityType.TREASURE_CAVALRY);
		Optional<TreasureCavalryEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		TreasureCavalryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TreasureCavalryActivityKVCfg.class);
		TreasureCavalryEntity entity = opEntity.get();
		if (entity.getRefreshTimes() >= kvCfg.getMaxRefresh()) {
			return false;
		}

		String costStr = kvCfg.getRefreshCost(entity.getRefreshTimes());
		Reward.RewardItem.Builder cost = RewardHelper.toRewardItem(costStr);
		// 拥有道具数
		boolean consumeResult = activity.getDataGeter().consumeItemsIsGold(playerId, Arrays.asList(cost), protocol.getType(), Action.TREASURE_CALVALRY_REFRESH);
		if (consumeResult == false) {
			return false;
		}

		entity.resetItems();
		entity.setMultiple(0);
		entity.setRefreshTimes(entity.getRefreshTimes() + 1);


		Builder resp = PBTreasuryRefreshResp.newBuilder().setPool(entity.getPool());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.TREASURE_CAVALRY_REFRESH_S, resp));
		activity.sync(playerId);

		return true;
	}

}
