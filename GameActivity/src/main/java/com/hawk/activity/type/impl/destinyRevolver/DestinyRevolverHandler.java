package com.hawk.activity.type.impl.destinyRevolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverCfg;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverFiveRewardCfg;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverNineRewardCfg;
import com.hawk.activity.type.impl.destinyRevolver.entity.DestinyRevolverEntity;
import com.hawk.game.protocol.Activity.DRFiveGachaResp;
import com.hawk.game.protocol.Activity.DRNineGachaReq;
import com.hawk.game.protocol.Activity.DRNineGachaResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 命运左轮
 * @author golden
 *
 */
public class DestinyRevolverHandler extends ActivityProtocolHandler {
	
	/**
	 * 第一次点击
	 */
	@ProtocolHandler(code = HP.code.DESTINY_REVOLVER_FIRST_KICK_NOTICE_VALUE)
	public void firstKick(HawkProtocol protocol, String playerId) {
		DestinyRevolverActivity activity = this.getActivity(ActivityType.DESTINY_REVOLVER);
		if (!activity.isOpening(playerId)) {
			return;
		}
		
		Optional<DestinyRevolverEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		entity.setFirstKick(true);
		
		activity.syncPageInfo(playerId);
		logger.info("destinyrevolver, firstKick, playerId:{}", playerId);
	}
	
	/**
	 * 抽五格的
	 */
	@ProtocolHandler(code = HP.code.DESTINY_REVOLVER_FIVE_GACHA_REQ_VALUE)
	public void gachaFive(HawkProtocol protocol, String playerId) {
		DestinyRevolverActivity activity = this.getActivity(ActivityType.DESTINY_REVOLVER);
		if (!activity.isOpening(playerId)) {
			return;
		}
		
		Optional<DestinyRevolverEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		
		DestinyRevolverCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DestinyRevolverCfg.class);
		
		List<Builder> cost = RewardHelper.toRewardItemList(kvCfg.getFiveCost());
		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, cost, protocol.getType(), Action.DESTINY_REVOLVER_FIVE_CONSUNE);
		if (!consumeResult) {
			return;
		}
		
		DRFiveGachaResp.Builder builder = DRFiveGachaResp.newBuilder();
		
		boolean isSpecial = false;
		
		int playerGachaFiveTimes = activity.addPlayerGachaFiveTimes(playerId);
		
		int fiveMaxjackpot = DestinyRevolverCfg.getInstance().getFiveMaxjackpot();
		if (playerGachaFiveTimes % fiveMaxjackpot == 0) {
			DestinyRevolverFiveRewardCfg specilCfg = null;
			
			ConfigIterator<DestinyRevolverFiveRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(DestinyRevolverFiveRewardCfg.class);
			while(it.hasNext()) {
				DestinyRevolverFiveRewardCfg cfg = it.next();
				if (cfg.getJackpot() != 1) {
					continue;
				}
				specilCfg = cfg;
				break;
			}
			
			if (specilCfg != null) {
				entity.setInTarot(true);
				entity.setNineEndTime(HawkTime.getMillisecond() + kvCfg.getNineCountdown());
				builder.setIsSpecialReward(true);
				builder.setCfgId(specilCfg.getId());
				logger.info("destinyrevolver, gachaFive special, playerId:{}", playerId);
			}
			
		} else {
			ConfigIterator<DestinyRevolverFiveRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(DestinyRevolverFiveRewardCfg.class);
			DestinyRevolverFiveRewardCfg cfg = HawkRand.randomWeightObject(it.toList());
			if (cfg.getJackpot() == 1) {
				isSpecial = true;
				entity.setInTarot(true);
				entity.setNineEndTime(HawkTime.getMillisecond() + kvCfg.getNineCountdown());
				logger.info("destinyrevolver, gachaFive special, playerId:{}", playerId);
			} else {
				ActivityReward rewards = new ActivityReward(cfg.getRewardList(), Action.DESTINY_REVOLVER_FIVE_AWARD);
				rewards.setAlert(true);
				rewards.setOrginType(RewardOrginType.DESTINY_REVOLVER_FIVE_REWARD, activity.getActivityId());
				activity.postReward(playerId, rewards);
				responseSuccess(playerId, protocol.getType());
			}
			builder.setIsSpecialReward(isSpecial);
			builder.setCfgId(cfg.getId());
		}
		
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DESTINY_REVOLVER_FIVE_GACHA_RESP_VALUE, builder));
		
		activity.syncPageInfo(playerId);
	}
	
	/**
	 * 抽九格的
	 */
	@ProtocolHandler(code = HP.code.DESTINY_REVOLVER_NINE_GACHA_REQ_VALUE)
	public void gachaNine(HawkProtocol protocol, String playerId) {
		DestinyRevolverActivity activity = this.getActivity(ActivityType.DESTINY_REVOLVER);
		if (!activity.isOpening(playerId)) {
			return;
		}
		
		Optional<DestinyRevolverEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		
		if (HawkTime.getMillisecond() > entity.getNineEndTime()) {
			return;
		}
		
		DRNineGachaReq req = protocol.parseProtocol(DRNineGachaReq.getDefaultInstance());
		int index = req.getIndex();
		if (index < 0 || index > 8) {
			return;
		}

		List<Integer> itemList = entity.getGridist();
		if (itemList.get(index) > 0) {
			return;
		}

		DestinyRevolverCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DestinyRevolverCfg.class);
		int openCount = 0; // 已翻
		int multipleCount = 0; // 已翻倍数道具数
		for (Integer i : itemList) {
			if (i.intValue() == 0) {
				continue;
			}
			openCount++;
			
			DestinyRevolverNineRewardCfg rcfg = HawkConfigManager.getInstance().getConfigByKey(DestinyRevolverNineRewardCfg.class, i);
			Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(rcfg.getReward());
			if (kvCfg.isMultipleItem(reward.getItemId())) {
				multipleCount++;
			}
		}
		if (openCount == 0 && entity.getMultiple() > 0) {
			entity.setMultiple(0);
		}
		boolean is4_0 = openCount == 7 && multipleCount == 0;

		ConfigIterator<DestinyRevolverNineRewardCfg> it = HawkConfigManager.getInstance().getConfigIterator(DestinyRevolverNineRewardCfg.class);
		// 剩余可抽宝藏
		List<DestinyRevolverNineRewardCfg> rlist = new ArrayList<>();
		List<DestinyRevolverNineRewardCfg> allLeft = new ArrayList<>();
		for (DestinyRevolverNineRewardCfg cfg : it) {
			if (itemList.contains(cfg.getId())) {
				continue;
			}
			allLeft.add(cfg);
			Reward.RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getReward());
			if (is4_0) {
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

		DestinyRevolverNineRewardCfg cfg = HawkRand.randomWeightObject(rlist);

		// 扣费
		Reward.RewardItem.Builder openCost = RewardHelper.toRewardItem(kvCfg.getTreasureCost(openCount));
		final int extryRewardCount = (int) openCost.getItemCount();
		// 拥有道具数
		int ticketCount = activity.getDataGeter().getItemNum(playerId, openCost.getItemId());
		List<Reward.RewardItem.Builder> cost = new ArrayList<>();
		if (ticketCount < openCost.getItemCount()) {
			long left = openCost.getItemCount() - ticketCount;
			Reward.RewardItem.Builder itemOnecePrice = RewardHelper.toRewardItem(kvCfg.getNinePrice());
			itemOnecePrice.setItemCount(itemOnecePrice.getItemCount() * left);
			openCost.setItemCount(ticketCount);

			cost.add(openCost);
			cost.add(itemOnecePrice);
		} else {
			cost.add(openCost);
		}

		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, cost, protocol.getType(), Action.DESTINY_REVOLVER_NINE_CONSUNE);
		if (consumeResult == false) {
			return;
		}
		itemList.set(index, cfg.getId());

		// 必得奖励
		if (StringUtils.isNotEmpty(kvCfg.getExtReward())) {
			List<RewardItem.Builder> result = RewardHelper.toRewardItemList(kvCfg.getExtReward());
			result.forEach(ite -> ite.setItemCount(ite.getItemCount() * extryRewardCount));
			ActivityReward reward = new ActivityReward(result, Action.DESTINY_REVOLVER_NINE_AWARD);
			reward.setAlert(false);
			reward.setOrginType(RewardOrginType.DESTINY_REVOLVER_NINE_REWARD, activity.getActivityId());
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
		
		ActivityReward rewards = new ActivityReward(rewardList, Action.DESTINY_REVOLVER_NINE_AWARD);
		rewards.setAlert(true);
		rewards.setOrginType(RewardOrginType.DESTINY_REVOLVER_NINE_REWARD, activity.getActivityId());
		activity.postReward(playerId, rewards);
		
		DRNineGachaResp.Builder builder = DRNineGachaResp.newBuilder();
		builder.setIndex(req.getIndex());
		builder.setRewardId(cfg.getId());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DESTINY_REVOLVER_NINE_GACHA_RESP_VALUE, builder));
		
		responseSuccess(playerId, protocol.getType());
		
		activity.syncPageInfo(playerId);
		
	}
	
	/**
	 * 放弃翻牌
	 */
	@ProtocolHandler(code = HP.code.DESTINY_REVOLVER_ABANDON_NINE_VALUE)
	public void abandon(HawkProtocol protocol, String playerId) {
		DestinyRevolverActivity activity = this.getActivity(ActivityType.DESTINY_REVOLVER);
		if (!activity.isOpening(playerId)) {
			return;
		}
		
		Optional<DestinyRevolverEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		entity.resetItems();
		activity.syncPageInfo(playerId);
		logger.info("destinyrevolver, abandon, playerId:{}", playerId);
	}
}
