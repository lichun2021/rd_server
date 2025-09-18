package com.hawk.activity.type.impl.hellfiretwo.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.hellfiretwo.HellFireTwoActivity;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;

public class HellFireTwoRankProvider extends AbstractActivityRankProvider<HellFireTwoRank> {
	/**
	 * 缓存
	 */
	private List<HellFireTwoRank> showList = new ArrayList<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.HELL_FIRE_TWO_RANK;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.HELL_FIRE_TWO_VALUE);
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
		Optional<HellFireTwoActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.HELL_FIRE_TWO_ACTIVITY.intValue());
		if (!opActivity.isPresent() || !opActivity.get().isInit()) {
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<HellFireTwoRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			HellFireTwoRank timeDropRank = new HellFireTwoRank();
			timeDropRank.setId(rank.getElement());
			timeDropRank.setRank(index);
			long score = RankScoreHelper.getRealScore((long) rank.getScore());
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
		throw  new UnsupportedOperationException("HellFireTwoRank can not addScore");   
	}

	@Override
	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	@Override
	public boolean insertRank(HellFireTwoRank rankInfo) {
		String playerId = rankInfo.getId();
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rankScore, playerId);
		
		return true;
	}

	@Override
	public List<HellFireTwoRank> getRankList() {
		return showList;
	}

	@Override
	public HellFireTwoRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = RankScoreHelper.getRealScore(index.getScore().longValue());
		}
		HellFireTwoRank strongestRank = new HellFireTwoRank();
		strongestRank.setId(id);
		strongestRank.setRank(rank);
		strongestRank.setScore(score);
		
		return strongestRank;
	}
		
	@Override
	public List<HellFireTwoRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}
		
	public String getRedisKey(String suffix) {
		 return ActivityRedisKey.HELL_FIRE_RANK_2+":"+suffix;
	}
	
	private String getRedisKey(){
		Optional<HellFireTwoActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.HELL_FIRE_TWO_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return getRedisKey(opActivity.get().getKeySuffix());
		} else {
			return getRedisKey("");
		}
		
	}

	@Override
	protected boolean canInsertIntoRank(HellFireTwoRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return ActivityHellFireTwoKVCfg.getInstance().getRankSize();
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
}
