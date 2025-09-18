package com.hawk.game.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.WharfUnloadEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WharfAwardCfg;
import com.hawk.game.config.WharfAwardPoolCfg;
import com.hawk.game.entity.WharfEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Wharf.WharfInfoSync;

/**
 * 码头逻辑
 * @author PhilChen
 *
 */
public class WharfService {

	static final Logger logger = LoggerFactory.getLogger("Server");

	private static WharfService service;

	private WharfService() {
	}

	public static WharfService getInstance() {
		if (service == null) {
			service = new WharfService();
		}
		return service;
	}

	/**
	 * 同步码头信息
	 */
	public void syncWharfInfo(Player player) {
		String conditionStr = ConstProperty.getInstance().getAirdropUnlock();
		String[] condition = conditionStr.split("_");
		if (condition.length < 2){
			return;
		}
		int buildingMaxLevel = player.getData().getBuildingMaxLevel(Integer.valueOf(condition[0]));
		if (buildingMaxLevel < Integer.valueOf(condition[1])) {
			return;
		}
		WharfEntity wharfEntity = player.getData().getWharfEntity();
		WharfInfoSync.Builder builder = WharfInfoSync.newBuilder();
		WharfAwardPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(WharfAwardPoolCfg.class, wharfEntity.getAwardPoolId());
		if (poolCfg != null && wharfEntity.isTookAward() == false) {
			List<Builder> rewardList = poolCfg.getRewardList();
			for (Builder rewardBuilder : rewardList) {
				builder.addReward(rewardBuilder.build());
			}
		}
		long now = HawkTime.getMillisecond();
		long lastRefreshTime = wharfEntity.getLastRefreshTime();
		long remainTime;
		if (now < lastRefreshTime) {
			remainTime = lastRefreshTime - now + wharfEntity.getAwardTime();
		} else {
			remainTime = wharfEntity.getAwardTime() - (now - lastRefreshTime);
		}
		builder.setRemainTime(remainTime);
		
		int configSize = HawkConfigManager.getInstance().getConfigSize(WharfAwardCfg.class);
		WharfAwardCfg theLastCfg = HawkConfigManager.getInstance().getConfigByIndex(WharfAwardCfg.class, configSize - 1);
		if (now < lastRefreshTime) {
			builder.setLastRefreshTime(lastRefreshTime - theLastCfg.getAwardMaxTime());
		} else {
			builder.setLastRefreshTime(lastRefreshTime);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WHARF_INFO_SYNC_S_VALUE, builder));
	}
	
	/**
	 * 刷新码头奖励
	 * @param player
	 */
	public void refreshWharfAward(Player player, WharfEntity wharfEntity) {
		WharfAwardCfg awardCfg = null;
		
		if (wharfEntity.getAwardId() > 0) {
			if (wharfEntity.isTookAward() == false) {
				return;
			}
			awardCfg = HawkConfigManager.getInstance().getConfigByKey(WharfAwardCfg.class, wharfEntity.getAwardId());
			if (awardCfg == null) {
				logger.error("[wharf] award config not exist! awardId : {}", wharfEntity.getAwardId());
				return;
			}
		}
		long now = HawkTime.getMillisecond();
		
		WharfAwardCfg nextAwardCfg = getNextAwardCfg(player, wharfEntity, awardCfg, now, false);
		if (nextAwardCfg == null) {
			return;
		}
		randomAward(player, wharfEntity, nextAwardCfg, now);
	}
	
	private WharfAwardCfg getNextAwardCfg(Player player, WharfEntity wharfEntity, WharfAwardCfg awardCfg, long now, boolean reset) {
		ConfigIterator<WharfAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WharfAwardCfg.class);
		int buildLevel = player.getCityLevel();
		int index = 0;
		WharfAwardCfg nextAwardCfg = null;
		if (reset == false && HawkTime.isSameDay(now, wharfEntity.getLastRefreshTime()) == false) {
			reset = true;
		}
		while (configIterator.hasNext()) {
			WharfAwardCfg cfg = configIterator.next();
			if (cfg.getFactoryMinLevel() <= buildLevel && buildLevel <= cfg.getFactoryMaxLevel()) {
				// 初始从第一个序号开始
				if (reset || wharfEntity.getAwardId() <= 0 && index == 0) {
					nextAwardCfg = cfg;
					break;
				}
				if (cfg.getAwardOrder() == awardCfg.getNextOrder()) {
					nextAwardCfg = cfg;
					break;
				}
				index++;
			}
		}
		if (nextAwardCfg == null) {
			logger.error("[wharf] award config not match! playerId: {}, buildLevel: {}, oldAwardId: {}", player.getId(), buildLevel, wharfEntity.getAwardId());
		}
		return nextAwardCfg;
	}
	
	/**
	 * 随机奖励
	 * @param player
	 * @param entity
	 * @param awardCfg
	 * @param now
	 */
	private void randomAward(Player player, WharfEntity entity, WharfAwardCfg awardCfg, long now) {
		WharfAwardPoolCfg random = randomAwardPool(awardCfg);
		long refreshTime = now;
		// 随机刷新时间
		int awardTime = HawkRand.randInt(awardCfg.getAwardMinTime(), awardCfg.getAwardMaxTime()) * 1000;
		long nextAM0Date = HawkTime.getNextAM0Date();
		if (nextAM0Date - now < awardTime) {
			awardCfg = getNextAwardCfg(player, entity, awardCfg, now, true);
			random = randomAwardPool(awardCfg);
			awardTime = HawkRand.randInt(awardCfg.getAwardMinTime(), awardCfg.getAwardMaxTime()) * 1000;
			refreshTime = nextAM0Date;
		}
		entity.setAwardId(awardCfg.getId());
		entity.setAwardPoolId(random.getId());
		entity.setAwardTime(awardTime);
		entity.setLastRefreshTime(refreshTime);
		entity.setTookAward(false);
		logger.debug("[wharf] refresh. playerId: {}, wharfId: {}, poolId: {}", player.getId(), awardCfg.getId(), random.getId());
	}

	private WharfAwardPoolCfg randomAwardPool(WharfAwardCfg awardCfg) {
		Map<WharfAwardPoolCfg, Integer> weightMap = new HashMap<>();
		ConfigIterator<WharfAwardPoolCfg> awardConfigs = HawkConfigManager.getInstance().getConfigIterator(WharfAwardPoolCfg.class);
		while (awardConfigs.hasNext()) {
			WharfAwardPoolCfg awardConfig = awardConfigs.next();
			if (awardConfig.getAwardId() != awardCfg.getAwardId()) {
				continue;
			}
			weightMap.put(awardConfig, awardConfig.getRate());
		}
		WharfAwardPoolCfg random = HawkRand.randomWeightObject(weightMap);
		if (random == null) {
			logger.error("[wharf] random award error! awardId: {}, weightMap: {}", awardCfg.getId(), weightMap);
		}
		return random;
	}
	
	/**
	 * 领取奖励
	 * @param player
	 * @return
	 */
	public Result<?> takeAward(Player player) {
		WharfEntity wharfEntity = player.getData().getWharfEntity();
		if (wharfEntity.getAwardPoolId() <= 0) {
			return Result.fail(Status.Error.WHARF_AWARD_NOT_EXIST_VALUE);
		}
		WharfAwardPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(WharfAwardPoolCfg.class, wharfEntity.getAwardPoolId());
		if (poolCfg == null) {
			return Result.fail(Status.Error.WHARF_AWARD_CONFIG_NOT_FOUND_VALUE);
		}
		if (wharfEntity.isTookAward()) {
			return Result.fail(Status.Error.WHARF_AWARD_WAS_TOOK_VALUE);
		}
		// 检查是否达到奖励领取时间
		long now = HawkTime.getMillisecond();
		long lastRefreshTime = wharfEntity.getLastRefreshTime();
		if (now < lastRefreshTime || now - lastRefreshTime < wharfEntity.getAwardTime()) {
			syncWharfInfo(player);
			return Result.fail(Status.Error.WHARF_AWARD_TIME_NOT_ACHIEVE_VALUE);
		}
		wharfEntity.setTookAward(true);
		
		// 时间到，可以领取奖励
		AwardItems awardItem = AwardItems.valueOf();
		List<Builder> rewardList = poolCfg.getRewardList();
		for (Builder builder : rewardList) {
			awardItem.addItem(builder.getItemType(), builder.getItemId(), (int) builder.getItemCount());
		}
		
		awardItem.rewardTakeAffectAndPush(player, Action.WHARF, false, RewardOrginType.WHARF_REWARD);
		String playerId = player.getId();
		// 刷新下一个奖励
		refreshWharfAward(player, wharfEntity);
		// 同步码头信息
		syncWharfInfo(player);
		// 活动：码头领取货物x次
		ActivityManager.getInstance().postEvent(new WharfUnloadEvent(playerId));
		
		logger.debug("[wharf] take award. playerId: {}, wharfId: {}, poolId: {}", playerId, wharfEntity.getAwardId(), poolCfg.getId());
		return Result.success();
	}
}
