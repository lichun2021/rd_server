package com.hawk.activity.type.impl.timeLimitDrop.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDropCfg;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

public class TimeLimitDropRankProvider extends AbstractActivityRankProvider<TimeLimitDropRank> {
	/**
	 * 缓存
	 */
	private List<TimeLimitDropRank> showList = new ArrayList<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.TIME_LIMIT_DROP_RANK;
	}

	@Override
	public boolean isFixTimeRank() {
		//只能放这里判断你是不是需要排序活动结束之后就不排序了
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.TIME_LIMIT_DROP_VALUE);
		if (opActivity.isPresent()) {
			return opActivity.get().isOpening("");
		} else {
			return false;
		}		
	}

	@Override
	public void loadRank() {
		this.doRankSort();		
	}

	@Override
	public void doRankSort() {
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<TimeLimitDropRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			TimeLimitDropRank timeDropRank = new TimeLimitDropRank();
			timeDropRank.setId(rank.getElement());
			timeDropRank.setRank(index);
			long score = (long) rank.getScore();
			timeDropRank.setScore(score);
			newRankList.add(timeDropRank);
			index++;
		}
		
		showList = newRankList;
	}

	@Override
	public List<TimeLimitDropRank> getRankList() {
		return showList;
	}

	@Override
	public TimeLimitDropRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		TimeLimitDropRank strongestRank = new TimeLimitDropRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		
		return strongestRank;
	}
	
	@Override
	public List<TimeLimitDropRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}

	@Override
	public void clean() {
		showList  = new ArrayList<>(this.getRankSize());
		ActivityLocalRedis.getInstance().del(getRedisKey());
	}

	@Override
	public void addScore(String id, int score) {
		throw  new UnsupportedOperationException("timeLimitDropRank can not addScore");
	}

	@Override
	public void remMember(String id) {		
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	@Override
	protected boolean canInsertIntoRank(TimeLimitDropRank rankInfo) {
		return true;
	}

	@Override
	protected boolean insertRank(TimeLimitDropRank rankInfo) {
		String playerId = rankInfo.getId();		
		ActivityLocalRedis.getInstance().zIncrby(getRedisKey(), playerId, rankInfo.getScore());
		
		return true;
	}

	@Override
	protected int getRankSize() {
		return TimeLimitDropCfg.getInstance().getRankSize();
	}
	
	public String getRedisKey(int termId) {
		return ActivityRedisKey.TIME_LIMIT_DROP_RANK+":"+termId; 
	}
	private String getRedisKey(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.TIME_LIMIT_DROP_VALUE);
		if (opActivity.isPresent()) {
			return getRedisKey(opActivity.get().getActivityTermId());
		} else {
			return getRedisKey(0);
		}		
		
	}
}
