package com.hawk.activity.type.impl.mergecompetition.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/merge_competition/%s/merge_competition_gift.xml", autoLoad=false, loadParams="368")
public class MergeCompetitionGiftCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/** 购买单个礼包消耗金条数区间 */
	private final int min;
	private final int max;
	
	/** 第几档 */
	private final int level;
	
	/** 给多少个宝箱 */
	private final int num;
	
	/** 单个宝箱的积分 */
	private final int point;
	
	/** 单个宝箱的奖励 */
	private final int reward;
	
	private static int maxLevel = 0;
	private static Map<Integer, Integer> rewardLevelMap = new HashMap<>();
	
	public MergeCompetitionGiftCfg() {
		id = 0;
		min = 0;
		max = 0;
		level = 0;
		num = 0;
		point = 0;
		reward = 0;
	}
	
	@Override
	protected boolean assemble() {
		maxLevel = Math.max(maxLevel, level);
		rewardLevelMap.put(reward, level);
		return true;
	}
	
	public int getId() {
		return id;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public int getLevel() {
		return level;
	}

	public int getNum() {
		return num;
	}

	public int getPoint() {
		return point;
	}

	public int getReward() {
		return reward;
	}

	public static int getMaxLevel() {
		return maxLevel;
	}
	
	public static int getLevelByRewardId(int rewardId) {
		return rewardLevelMap.getOrDefault(rewardId, 0);
	}
}
