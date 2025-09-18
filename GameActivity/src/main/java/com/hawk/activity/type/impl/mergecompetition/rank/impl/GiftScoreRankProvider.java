package com.hawk.activity.type.impl.mergecompetition.rank.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hawk.log.HawkLog;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionActivity;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionConstCfg;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRank;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.LogConst.LogInfoType;

import redis.clients.jedis.Tuple;

/**
 * 嘉奖积分排行榜
 */
public class GiftScoreRankProvider extends MergeCompetitionRankProvider {
	/**
	 * 待添加积分的人员
	 */
	volatile Map<String, Double> addScoreMap = new ConcurrentHashMap<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.MERGE_COMPETITION_GIFT_SCORE_RANK;
	}
	
	@Override
	public int getActivityRankType() {
		return MergeCompetitionConst.RANK_TYPE_GIFT_SCORE;
	}

	@Override
	public boolean insertRank(MergeCompetitionRank rankInfo) {
		String elementId = getRankElementId(rankInfo.getId());
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		addScoreMap.put(elementId, Double.valueOf(rankScore));
		return true;
	}
	
	@Override
	public void refreshCacheData() {
		Map<String, Double> map = addScoreMap;
		addScoreMap = new ConcurrentHashMap<>();
		if (!map.isEmpty()) {
			getRedis().zAdd(getRedisKey(), map, getRedisExpire());
		}
	}
	
	@Override
	public void doRankSort() {
		MergeCompetitionActivity activity = getActivity();
		if (activity == null || activity.isHidden("")) {
			return;
		}
		
		HawkLog.logPrintln("MergeCompetitionActivity rankSort: {}", this.getRankTypeStr());
		
		Map<String, Double> map = addScoreMap;
		this.refreshCacheData();
		String oppServerId = "", serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		int rankSize = this.getRankSize();
		Set<Tuple> rankSet = getRedis().zRevrangeWithScores(this.getRedisKey(), 0, Math.max((rankSize - 1), 0), getRedisExpire());		
		List<MergeCompetitionRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		List<String> memberIds = new ArrayList<>();
		for (Tuple rank : rankSet) {
			String[] elements = rank.getElement().split(":");
			String rankServerId = elements[0];
			String memberId = elements[1];
			if (!serverId.equals(rankServerId)) {
				oppServerId = rankServerId;
			}
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
			MergeCompetitionRank rankData = new MergeCompetitionRank();
			rankData.setId(memberId);
			rankData.setRank(index);
			rankData.setScore(score);
			newRankList.add(rankData);
			memberIds.add(memberId);
			if (map.containsKey(rank.getElement())) {
				logGiftScoreRankCalc(memberId, index, score);
			}
			index++;
		}
		
		this.resetShowList(newRankList);
		this.contributeServerScore(this.getActivityRankType(), Collections.emptyMap(), memberIds, oppServerId);
	}
	
	/**
	 * 三大比拼排行榜结算
	 * @param memberId
	 * @param rankServerId
	 * @param rank
	 * @param point
	 */
	private void logGiftScoreRankCalc(String playerId, int rank, long score) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				String guildId = ActivityManager.getInstance().getDataGeter().getGuildId(playerId);
				Map<String, Object> param = new HashMap<>();
				param.put("rank", rank);     //排名
				param.put("score", score);   //该玩家的嘉奖积分
				param.put("guildId", guildId == null ? "" : guildId); //玩家所属联盟id
				ActivityManager.getInstance().getDataGeter().logActivityCommon(playerId, LogInfoType.merge_compete_gift_score, param);
				return null;
			}
		});
	}

	@Override
	public int getRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType4ShowMax();
	}
	
	@Override
	public int getLocalRankSize() {
		return 0;
	}

	@Override
	public String getRedisKey() {
		return MergeCompetitionConst.RANK_GIFT_SCORE + getActivity().getServerGroup();
	}

	@Override
	public String getLocalRedisKey(String serverId) {
		return "";
	}

	@Override
	public String getRankTypeStr() {
		return "giftScore";
	}
	
}
