package com.hawk.activity.type.impl.mergecompetition.rank.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionConstCfg;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRank;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.gamelib.rank.RankScoreHelper;

/**
 * 个人去兵战力排行榜
 */
public class PersonalPowerRankProvider extends MergeCompetitionRankProvider {
	/**
	 * 玩家去兵战力变更表
	 */
	volatile Map<String, Double> noArmyPowerChangeMap = new ConcurrentHashMap<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.MERGE_COMPETITION_PERSON_POWER_RANK;
	}
	
	@Override
	public int getActivityRankType() {
		return MergeCompetitionConst.RANK_TYPE_PERSON_POWER;
	}

	@Override
	public boolean insertRank(MergeCompetitionRank rankInfo) {
		String elementId = getRankElementId(rankInfo.getId());
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		noArmyPowerChangeMap.put(elementId, Double.valueOf(rankScore));
		return true;
	}
	
	@Override
	public void refreshCacheData() {
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Map<String, Double> noArmyMap = noArmyPowerChangeMap;
		noArmyPowerChangeMap = new ConcurrentHashMap<>();
		if (!noArmyMap.isEmpty()) {
			getRedis().zAdd(getRedisKey(), noArmyMap, getRedisExpire());
			getRedis().zAdd(getLocalRedisKey(serverId), noArmyMap, getRedisExpire());
		}
	}
	
	@Override
	public int getRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType1ShowMax();
	}
	
	@Override
	public int getLocalRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType1LocalNum();
	}

	@Override
	public String getRedisKey() {
		return MergeCompetitionConst.RANK_POWER_PERSON + getActivity().getServerGroup();
	}

	@Override
	public String getLocalRedisKey(String serverId) {
		return MergeCompetitionConst.RANK_POWER_PERSON_LOCAL + serverId;
	}

	@Override
	public String getRankTypeStr() {
		return "playerNoArmyPower";
	}
}
