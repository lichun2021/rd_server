package com.hawk.activity.type.impl.bestprize.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import com.hawk.serialize.string.SerializeHelper;
	
@HawkConfigManager.XmlResource(file = "activity/the_best_prize/the_best_prize_pool.xml")
public class BestPrizePoolCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 初始小奖池数量
	 */
	private final int startPoolValue;
	/**
	 * 奖励内容：a-g奖
	 */
	private final String rewards;
	/**
	 * 抽取消耗（活动结束要兑换回收的）
	 */
	private final String drawConsume;
	/**
	 * 抽取消耗（活动结束可以保留的）
	 */
	private final String drawReserveConsume;
	/**
	 * 抽取消耗数量
	 */
	private final int itemNeedValue;
	
	/**
	 * 最终奖
	 */
	private final String lastRewards;
	/**
	 * 最终奖转换积分
	 */
	private final String lastRewardChangePoints;
	/**
	 * 动态增加奖池，单日可增加奖池上限
	 */
	private final int poolLimit;
	
	private final int maxGetLimit;
	
	private Map<Integer, Integer> rewardWeightMap = new HashMap<>();
	
	/**
	 * 构造
	 */
	public BestPrizePoolCfg() {
		id = 0;
		startPoolValue = 0;
		rewards = "";
		drawConsume = "";
		drawReserveConsume = "";
		itemNeedValue = 1;
		lastRewards = "";
		lastRewardChangePoints = "";
		poolLimit = 0;
		maxGetLimit = 0;
	}
	
	public int getId() {
		return id;
	}
	
	public int getStartPoolValue() {
		return startPoolValue;
	}
	
	public String getRewards() {
		return rewards;
	}
	
	public String getDrawConsume() {
		return drawConsume;
	}
	
	public String getDrawReserveConsume() {
		return drawReserveConsume;
	}
	
	public int getItemNeedValue() {
		return itemNeedValue;
	}

	public String getLastRewards() {
		return lastRewards;
	}
	
	public String getLastRewardChangePoints() {
		return lastRewardChangePoints;
	}
	
	public boolean assemble() {
		rewardWeightMap = SerializeHelper.stringToMap(rewards, Integer.class, Integer.class, "_", ",");
		return true;
	}

	public Map<Integer, Integer> getRewardWeightMap() {
		return rewardWeightMap;
	}

	public int getPoolLimit() {
		return poolLimit;
	}
	
	public int getMaxGetLimit() {
		return maxGetLimit;
	}
	
	public static boolean doCheck() {
		ConfigIterator<BestPrizePoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BestPrizePoolCfg.class);
		while (iterator.hasNext()) {
			BestPrizePoolCfg cfg = iterator.next();
			for (int rewardId : cfg.getRewardWeightMap().keySet()) {
				BestPrizePoolAwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolAwardCfg.class, rewardId);
				if (awardCfg == null || awardCfg.getPoolId() != cfg.getId()) {
					throw new RuntimeException("the_best_prize_pool.xml -> awardId not match the poolId, awardId: " + rewardId + ", poolId: " + cfg.getId());
				}
			}
		}
		return true;
	}
	
}
