package com.hawk.activity.type.impl.heroSkin;

import java.util.ArrayList;
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
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinActivityKVCfg;
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinRewardCfg;
import com.hawk.activity.type.impl.heroSkin.entity.HeroSkinEntity;
import com.hawk.game.protocol.Activity.PBHeroSkinOpen;
import com.hawk.game.protocol.Activity.PBHeroSkinRefreshResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 英雄皮肤活动
 * @author golden
 *
 */
public class HeroSkinHandler extends ActivityProtocolHandler {

	/**
	 * 同步
	 */
	@ProtocolHandler(code = HP.code.HERO_SKIN_INFO_C_VALUE)
	public boolean onReqInfo(HawkProtocol protocol, String playerId) {
		HeroSkinActivity activity = getActivity(ActivityType.HERO_SKIN);
		activity.sync(playerId);
		return true;
	}

	/**
	 * 翻
	 */
	@ProtocolHandler(code = HP.code.HERO_SKIN_OPEN_VALUE)
	public boolean onOpen(HawkProtocol protocol, String playerId) {
		PBHeroSkinOpen req = protocol.parseProtocol(PBHeroSkinOpen.getDefaultInstance());
		int index = req.getIndex();
		if (index < 0 || index > 8) {
			return false;
		}
		HeroSkinActivity activity = getActivity(ActivityType.HERO_SKIN);
		Optional<HeroSkinEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		activity.sync(playerId);
		HeroSkinEntity entity = opEntity.get();
		List<Integer> itemList = entity.getItemsList();
		if (itemList.get(index) > 0) {
			return false;
		}

		HeroSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HeroSkinActivityKVCfg.class);
		int openCount = 0; // 已翻
		int multipleCount = 0; // 已翻倍数道具数
		for (Integer i : itemList) {
			if (i.intValue() == 0) {
				continue;
			}
			openCount++;
			HeroSkinRewardCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(HeroSkinRewardCfg.class, i);
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

		ConfigIterator<HeroSkinRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(HeroSkinRewardCfg.class);
		// 剩余可抽宝藏
		List<HeroSkinRewardCfg> rlist = new ArrayList<>();
		List<HeroSkinRewardCfg> allLeft = new ArrayList<>();
		for (HeroSkinRewardCfg cfg : it) {
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

		HeroSkinRewardCfg cfg = HawkRand.randomWeightObject(rlist);

		// 扣费
		List<Builder> openCost = RewardHelper.toRewardItemList(kvCfg.getTreasureCost(openCount));
		
		int extryRewardCount = 0;
		if (openCost.size() > 0) {
			extryRewardCount = (int)openCost.get(0).getItemCount();
		}
		final int extryRewardFinalC = extryRewardCount;

		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, openCost, protocol.getType(), Action.HERO_SKIN_OPEN);
		if (consumeResult == false) {
			return false;
		}
		itemList.set(index, cfg.getId());

//		// 必得奖励
		if (StringUtils.isNotEmpty(kvCfg.getExtReward())) {
			List<RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExtReward());
			result.forEach(ite -> ite.setItemCount(ite.getItemCount() * extryRewardFinalC));
			ActivityReward reward = new ActivityReward(result, Action.HERO_SKIN_OPEN);
			reward.setAlert(false);
			reward.setOrginType(RewardOrginType.HERO_SKIN_REWARD, activity.getActivityId());
			activity.postReward(playerId, reward);
		}
		
		// 如果是最终奖励
		if (cfg.isFinallyReward()) {
			entity.setHasFinally(1);
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
		ActivityReward rewards = new ActivityReward(rewardList, Action.HERO_SKIN_OPEN);
		rewards.setAlert(true);
		rewards.setOrginType(RewardOrginType.HERO_SKIN_REWARD, activity.getActivityId());
		activity.postReward(playerId, rewards);
		activity.sync(playerId);
		responseSuccess(playerId, protocol.getType());
		
		logger.info("HERO_SKIN_OPEN playerId={} index={} record={} reward={}", playerId, index, rewardList, RewardHelper.toItemString(reward.build()));
		return true;
	}

	/**
	 * 刷新
	 */
	@ProtocolHandler(code = HP.code.HERO_SKIN_REFRESH_C_VALUE)
	public boolean onRefresh(HawkProtocol protocol, String playerId) {
		HeroSkinActivity activity = getActivity(ActivityType.HERO_SKIN);
		Optional<HeroSkinEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		
		HeroSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HeroSkinActivityKVCfg.class);
		HeroSkinEntity entity = opEntity.get();
		if (entity.getRefreshTimes() >= kvCfg.getMaxRefresh()) {
			return false;
		}

		if (entity.hasFinally()) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.HERO_SKIN_ACTI_HAS_FINALLY_VALUE);
			return false;
		}
		
		String costStr = kvCfg.getRefreshCost(entity.getRefreshTimes());
		List<Builder> cost = RewardHelper.toRewardItemList(costStr);
		// 拥有道具数
		boolean consumeResult = activity.getDataGeter().consumeItemsIsGold(playerId, cost, protocol.getType(), Action.HERO_SKIN_REFRESH);
		if (consumeResult == false) {
			return false;
		}

		entity.resetItems();
		entity.setMultiple(0);
		entity.setRefreshTimes(entity.getRefreshTimes() + 1);


		PBHeroSkinRefreshResp.Builder resp = PBHeroSkinRefreshResp.newBuilder();
		resp.setPool(entity.getPool());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HERO_SKIN_REFRESH_S, resp));
		activity.sync(playerId);

		return true;
	}

}
