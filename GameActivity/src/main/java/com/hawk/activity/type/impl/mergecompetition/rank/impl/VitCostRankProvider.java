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
 * 个人体力消耗排行榜
 */
public class VitCostRankProvider extends MergeCompetitionRankProvider {
	/**
	 * 玩家体力消耗
	 */
	volatile Map<String, Double> vitCostMap = new ConcurrentHashMap<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.MERGE_COMPETITION_VITCOST_RANK;
	}
	
	@Override
	public int getActivityRankType() {
		return MergeCompetitionConst.RANK_TYPE_VIT_COST;
	}

	@Override
	public boolean insertRank(MergeCompetitionRank rankInfo) {
		String elementId = getRankElementId(rankInfo.getId());
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		vitCostMap.put(elementId, Double.valueOf(rankScore));
		return true;
	}
	
	@Override
	public void refreshCacheData() {
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Map<String, Double> map = vitCostMap;
		vitCostMap = new ConcurrentHashMap<>();
		if (!map.isEmpty()) {
			getRedis().zAdd(getRedisKey(), map, getRedisExpire());
			getRedis().zAdd(getLocalRedisKey(serverId), map, getRedisExpire());
		}
	}

	@Override
	public int getRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType3ShowMax();
	}
	
	@Override
	public int getLocalRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType3LocalNum();
	}

	@Override
	public String getRedisKey() {
		return MergeCompetitionConst.RANK_VIT_PERSON + getActivity().getServerGroup();
	}

	@Override
	public String getLocalRedisKey(String serverId) {
		return MergeCompetitionConst.RANK_VIT_PERSON_LOCAL + serverId;
	}
	
	@Override
	public String getRankTypeStr() {
		return "vitCost";
	}
}
