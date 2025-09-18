package com.hawk.activity.type.impl.hellfire.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.hellfire.HellFireActivity;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;

import redis.clients.jedis.Tuple;

public class HellFireRankProvider extends AbstractActivityRankProvider<HellFireRank> {
	/**
	 * 缓存
	 */
	private List<HellFireRank> showList = new ArrayList<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.HELL_FIRE_RANK;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(com.hawk.game.protocol.Activity.ActivityType.HELL_FIRE_VALUE);
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
		Optional<HellFireActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.HELL_FIRE_ACTIVITY.intValue());
		if (!opActivity.isPresent() || !opActivity.get().isInit()) {
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<HellFireRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			HellFireRank timeDropRank = new HellFireRank();
			timeDropRank.setId(rank.getElement());
			timeDropRank.setRank(index);
			long score = (long)rank.getScore();
			timeDropRank.setScore(score);
			newRankList.add(timeDropRank);
			index++;
		}
		
		showList = newRankList;		
	}

	@Override
	public void clean() {
		showList  = new ArrayList<>(this.getRankSize());
		ActivityLocalRedis.getInstance().del(getRedisKey());		
	}

	@Override
	public void addScore(String id, int score) {		
		throw  new UnsupportedOperationException("HellFireRank can not addScore");   
	}

	@Override
	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	@Override
	public boolean insertRank(HellFireRank rankInfo) {
		String playerId = rankInfo.getId();
		ActivityLocalRedis.getInstance().zIncrby(getRedisKey(), playerId, rankInfo.getScore());
		
		return true;
	}

	@Override
	public List<HellFireRank> getRankList() {
		return showList;
	}

	@Override
	public HellFireRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		HellFireRank strongestRank = new HellFireRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		
		return strongestRank;
	}
		
	@Override
	public List<HellFireRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}
	
	public String getRedisKey(String suffix) {
		 return ActivityRedisKey.HELL_FIRE_RANK+":"+suffix;
	}
	
	private String getRedisKey(){
		Optional<HellFireActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.HELL_FIRE_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return getRedisKey(opActivity.get().getKeySuffix());
		} else {
			return getRedisKey("");
		}
		
	}

	@Override
	protected boolean canInsertIntoRank(HellFireRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return ActivityHellFireKVCfg.getInstance().getRankSize();
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
}
