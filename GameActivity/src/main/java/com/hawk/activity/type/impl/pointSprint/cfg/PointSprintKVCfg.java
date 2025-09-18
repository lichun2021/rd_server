package com.hawk.activity.type.impl.pointSprint.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 巅峰荣耀
 * @author Golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/point_sprint/point_sprint_kv.xml")
public class PointSprintKVCfg extends HawkConfigBase {
	/**
	 * 起服延迟开放时间
	 */
	private final int serverDelay;
	
	/**
	 * 排名奖励保底积分要求名次
	 */
	private final int awardLimitRank;
	
	/**
	 * 排名奖励保底积分值
	 */
	private final String rankAwardLimitPoints;
	
	/**
	 * 排行榜大小
	 */
	private final int rankSize;
	
	/**
	 * 微信分组数量
	 */
	private final int groupAmount;
	/**
	 * qq分组数量
	 */
	private final int groupQQAmount;
	
	
	/**
	 * 排名奖励保底积分值
	 */
	private Map<Integer, Integer> rankAwardLimitPointsMap;
	
	/**
	 * 单例
	 */
	private static PointSprintKVCfg instance;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static PointSprintKVCfg getInstance(){
		return instance;
	}
	
	
	/**
	 * 构造
	 */
	public PointSprintKVCfg(){
		serverDelay = 0;
		awardLimitRank = 3;
		rankAwardLimitPoints = "";
		rankSize = 100;
		groupAmount = 0;
		groupQQAmount = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getAwardLimitRank() {
		return awardLimitRank;
	}

	public int getRankSize() {
		return rankSize;
	}
	
	public int getRankAwardLimitPoints(int rank) {
		return rankAwardLimitPointsMap.get(rank);
	}

	public int limitPointsRankSize() {
		return rankAwardLimitPointsMap.size();
	}
	
	@Override
	protected boolean assemble() {
		if (groupAmount <= 0 || groupQQAmount <= 0) {
			return false;
		}
		
		Map<Integer, Integer> rankAwardLimitPointsMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(rankAwardLimitPoints)) {
			String[] split = rankAwardLimitPoints.split("_");
			for (int i = 0; i < split.length; i++) {
				rankAwardLimitPointsMap.put(i + 1, Integer.valueOf(split[i]));
			}
		}
		this.rankAwardLimitPointsMap = rankAwardLimitPointsMap;
		return true;
	}

	public int getGroupAmount() {
		return groupAmount;
	}

	public int getGroupQQAmount() {
		return groupQQAmount;
	}
}
