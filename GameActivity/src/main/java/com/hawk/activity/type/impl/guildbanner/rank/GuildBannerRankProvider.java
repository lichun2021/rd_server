package com.hawk.activity.type.impl.guildbanner.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.guildbanner.GuildBannerActivity;
import com.hawk.activity.type.impl.guildbanner.cfg.ActivityGuildBannerKVCfg;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;

public class GuildBannerRankProvider extends AbstractActivityRankProvider<GuildBannerRank> {
	/**
	 * 缓存
	 */
	private List<GuildBannerRank> showList = new ArrayList<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.GUILD_BANNER_RANK;
	}

	@Override
	public boolean isFixTimeRank() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.GUILD_BANNER_VALUE);
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
		Optional<GuildBannerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.GUILD_BANNER_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<GuildBannerRank> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			GuildBannerRank timeDropRank = new GuildBannerRank();
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
		throw  new UnsupportedOperationException("GuildBannerRank can not addScore");   
	}

	@Override
	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	@Override
	public boolean insertRank(GuildBannerRank rankInfo) {
		String playerId = rankInfo.getId();
		long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rankScore, playerId);
		return true;
	}

	@Override
	public List<GuildBannerRank> getRankList() {
		return showList;
	}

	@Override
	public GuildBannerRank getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = RankScoreHelper.getRealScore(index.getScore().longValue());
		}
		
		GuildBannerRank bannerRank = new GuildBannerRank();
		bannerRank.setId(id);
		bannerRank.setRank(rank);
		bannerRank.setScore(score);
		
		return bannerRank;
	}
		
	@Override
	public List<GuildBannerRank> getRanks(int start, int end) {
		start = start < 0 ? 0 : start;
		end = end > showList.size() ? showList.size() : end;
		
		return showList.subList(start, end);
	}
		
	public String getRedisKey(String suffix) {
		 return ActivityRedisKey.GUILD_BANNER_RANK + ":" + suffix;
	}
	
	private String getRedisKey(){
		Optional<GuildBannerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(com.hawk.activity.type.ActivityType.GUILD_BANNER_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return getRedisKey(opActivity.get().getKeySuffix());
		} else {
			return getRedisKey("");
		}
		
	}

	@Override
	protected boolean canInsertIntoRank(GuildBannerRank rankInfo) {
		return true;
	}

	@Override
	protected int getRankSize() {
		return ActivityGuildBannerKVCfg.getInstance().getRankSize();
	}
	
	public void cleanShowList() {
		this.showList = new ArrayList<>();
	}
}
