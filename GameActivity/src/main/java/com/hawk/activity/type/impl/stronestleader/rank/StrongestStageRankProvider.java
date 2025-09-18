package com.hawk.activity.type.impl.stronestleader.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.stronestleader.StrongestLeaderActivity;

import redis.clients.jedis.Tuple;

/**
 * 最强指挥官阶段排名
 * @author PhilChen
 *
 */
public class StrongestStageRankProvider extends AbstractActivityRankProvider<StrongestRank> {

	private List<StrongestRank> showRankList = new ArrayList<StrongestRank>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.STRONGEST_STAGE_RANK;
	}
	
	protected String getRedisKey() {
		Optional<StrongestLeaderActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.STRONGEST_LEADER.intValue());
		String key = ActivityRedisKey.STRONGEST_STAGE_RANK;
		if (opActivity.isPresent()) {
			key = key + ":" + opActivity.get().getStageId();
		}
		return key;
	}
	
	@Override
	public boolean isFixTimeRank() {
		return true;
	}
	
	@Override
	public void loadRank() {
		doRankSort();
	}
	
	@Override
	protected int getRankSize() {
		return ActivityConfig.getInstance().getActivityCircularRankSize();
	}

	@Override
	public void doRankSort() {
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));

		showRankList.clear();

		int index = 1;
		for (Tuple rank : rankSet) {
			StrongestRank strongestRank = new StrongestRank();
			strongestRank.setId(rank.getElement());
			strongestRank.setRank(index);
			long score = (long) rank.getScore();
			strongestRank.setScore(score);
			showRankList.add(strongestRank);
			index++;
		}
	}
	
	@Override
	public void addScore(String id, int score) {
	}
	
	@Override
	protected boolean canInsertIntoRank(StrongestRank rankInfo) {
		return true;
	}

	@Override
	public boolean insertRank(StrongestRank rank) {
		String playerId = rank.getId();
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rank.getScore(), playerId);
		return true;
	}

	@Override
	public List<StrongestRank> getRankList() {
		return showRankList;
	}

	@Override
	public StrongestRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		StrongestRank strongestRank = new StrongestRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		return strongestRank;
	}

	@Override
	public List<StrongestRank> getRanks(int start, int end) {
		if (end > 0) {
			end = end - 1;
		}
		List<StrongestRank> list = new ArrayList<>();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), start - 1, end);
		int rank = start;
		for (Tuple tuple : set) {
			long score = (long) tuple.getScore();
			list.add(StrongestRank.valueOf(tuple.getElement(), score, rank));
			rank++;
		}
		return list;
	}

	@Override
	public void clean() {
		showRankList.clear();
		ActivityLocalRedis.getInstance().del(getRedisKey());
	}

	@Override
	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

}
