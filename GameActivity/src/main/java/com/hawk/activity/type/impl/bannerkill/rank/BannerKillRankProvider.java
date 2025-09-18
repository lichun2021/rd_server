package com.hawk.activity.type.impl.bannerkill.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.bannerkill.BannerKillActivity;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

public class BannerKillRankProvider extends AbstractActivityRankProvider<BannerKillRank> {
	/**
	 * 缓存
	 */
	private List<BannerKillRank> showList = new ArrayList<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.BANNER_KILL_RANK;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.BANNER_KILL_ENEMY_VALUE);
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
		Optional<BannerKillActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.BANNER_KILL_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<BannerKillRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			BannerKillRank timeDropRank = new BannerKillRank();
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
	public void clean() {
		showList  = new ArrayList<>(this.getRankSize());
		ActivityLocalRedis.getInstance().del(getRedisKey());		
	}

	@Override
	public void addScore(String id, int score) {		
		throw  new UnsupportedOperationException("BannerKillRank can not addScore");   
	}

	@Override
	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	@Override
	public boolean insertRank(BannerKillRank rankInfo) {
		String playerId = rankInfo.getId();
		long rankScore = rankInfo.getScore();
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rankScore, playerId);
		return true;
	}

	@Override
	public List<BannerKillRank> getRankList() {
		return showList;
	}

	@Override
	public BannerKillRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		
		BannerKillRank bannerRank = new BannerKillRank();
		bannerRank.setId(id);
		bannerRank.setRank(rank);
		bannerRank.setScore(score);
		
		return bannerRank;
	}
		
	@Override
	public List<BannerKillRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}
		
	public String getRedisKey(String suffix) {
		 return ActivityRedisKey.BANNER_KILL_RANK + ":" + suffix;
	}
	
	private String getRedisKey(){
		Optional<BannerKillActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.BANNER_KILL_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return getRedisKey(opActivity.get().getKeySuffix());
		} else {
			return getRedisKey("");
		}
		
	}

	@Override
	protected boolean canInsertIntoRank(BannerKillRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return ActivityBannerKillKVCfg.getInstance().getRankSize();
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
}
