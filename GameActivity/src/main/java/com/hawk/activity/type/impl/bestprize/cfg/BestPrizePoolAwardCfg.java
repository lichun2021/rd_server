package com.hawk.activity.type.impl.bestprize.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
	
@HawkConfigManager.XmlResource(file = "activity/the_best_prize/the_best_prize_poolRewards.xml")
public class BestPrizePoolAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 奖励内容：a-g奖
	 */
	private final String rewards;
	/**
	 * 抽取次数
	 */
	private final int limitTimes;
	/**
	 * 转换积分
	 */
	private final String changePoints;
	/**
	 * 属于哪个大池子
	 */
	private final int poolId;
	/**
	 * 是否是A将
	 */
	private final int bigPrize;
	
	private static Map<Integer, Integer> poolLimitTimesMap = new HashMap<>();
	/**
	 * 每个奖池下初始A奖数量
	 */
	private static Map<Integer, Integer> poolRewardACountMap = new HashMap<>();
	
	/**
	 * 构造
	 */
	public BestPrizePoolAwardCfg() {
		id = 0;
		rewards = "";
		limitTimes = 0;
		changePoints = "";
		poolId = 0;
		bigPrize = 0;
	}
	
	public boolean assemble() {
		int count = poolLimitTimesMap.getOrDefault(poolId, 0);
		poolLimitTimesMap.put(poolId, count + limitTimes);
		if (bigPrize > 0) {
			poolRewardACountMap.put(poolId, limitTimes);
		}
		return true;
	}
	
	public int getId() {
		return id;
	}
	
	public String getRewards() {
		return rewards;
	}

	public String getChangePoints() {
		return changePoints;
	}

	public int getLimitTimes() {
		return limitTimes;
	}
	
	public int getPoolId() {
		return poolId;
	}
	
	public int getBigPrize() {
		return bigPrize;
	}

	public static int getlimitTimesByPoolId(int poolId) {
		return poolLimitTimesMap.getOrDefault(poolId, 0);
	}
	
	public static int getLimit(int cfgId) {
		BestPrizePoolAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolAwardCfg.class, cfgId);
		return cfg == null ? 0 : cfg.getLimitTimes();
	}
	
	public static int getPoolRewardACount(int poolId) {
		return poolRewardACountMap.getOrDefault(poolId, 0);
	}
}
