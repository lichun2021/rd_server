package com.hawk.activity.type.impl.powercollect.rank.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.powercollect.PowerCollectActivity;
import com.hawk.activity.type.impl.powercollect.cache.GuildScore;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectKVCfg;
import com.hawk.activity.type.impl.powercollect.rank.PowerCollectRank;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

public class GuildRank implements PowerCollectRank<GuildScore> {
	
	//private Map<String, GuildScore> map = new ConcurrentHashMap<>();
	
	private List<GuildScore> rankList = new ArrayList<>();
	
	@Override
	public String getKey() {
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.POWER_COLLECT_GUILD_RANK, termId);
		return key;
	}

	@Override
	public void doRankSort() {
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(getKey(), 0, Math.max((rankSize - 1), 0));
		for(Tuple tuple : set){
			String guildId = tuple.getElement();
			if(!isExistGuild(guildId)){
				HawkLog.logPrintln("PowerCollectRank GuildRank guild not exist, guildid: {}", guildId);
				continue;
			}
		}
		List<GuildScore> rankList = new ArrayList<>(rankSize);
		for(Tuple tuple : set){
			GuildScore guildData = new GuildScore(tuple.getElement());
			//long realScore = calRealScore((long)tuple.getScore());
			long realScore = (long)tuple.getScore();
			guildData.setAl(new AtomicLong(realScore));
			rankList.add(guildData);
		}
		this.rankList = rankList;
	}

	@Override
	public boolean addScore(double score, String member) {
		String key = getKey();
		if(member == null || score == 0){
			return false;
		}
		String guildId = member;
		if (!isExistGuild(member)) {
			HawkLog.logPrintln("PowerCollectRank GuildRank add score guild not exist, guildid: {}", guildId);
			return false;
		}
//		GuildScore guildScore = map.get(guildId);
//		if(guildScore == null){
//			guildScore = loadRankData(guildId);
//		}
//		guildScore.addScore(score);
//		long exchangeScore = exchangeScore(guildScore.getScore());
//		ActivityLocalRedis.getInstance().zaddWithExpire(key, exchangeScore, guildId, PowerCollectActivity.expireTime);
		ActivityLocalRedis.getInstance().zIncrbyWithExpire(key, guildId, score, PowerCollectActivity.expireTime);
		return true;
	}

	@Override
	public void clear() {
		//map.clear();
	}

	@Override
	public synchronized GuildScore loadRankData(String guildId) {
//		GuildScore guildScore = map.get(guildId);
//		if(guildScore != null){
//			return guildScore;
//		}
//		synchronized (this) {
//			Double score = ActivityLocalRedis.getInstance().zScore(getKey(), guildId);
//			if(score == null){
//				guildScore = new GuildScore(guildId);
//				guildScore.setAl(new AtomicLong(0));
//				map.put(guildId, guildScore);
//				return guildScore;
//			}else{
//				guildScore = new GuildScore(guildId);
//				guildScore.setAl(new AtomicLong(calRealScore(score.longValue())));
//				map.put(guildId, guildScore);
//				return guildScore;
//			}
//		}
		
		return null;
	}

	@Override
	public List<GuildScore> getRankList() {
		return rankList;
	}

	@Override
	public int getRankSize() {
		PowerCollectKVCfg config = HawkConfigManager.getInstance().getKVInstance(PowerCollectKVCfg.class);
		return config.getGuildRankSize();
	}

	@Override
	public List<GuildScore> getHasRewardRankList(int termId, int maxSize) {
		String key = String.format(ActivityRedisKey.POWER_COLLECT_GUILD_RANK, termId);
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((rankSize - 1), 0));
		List<GuildScore> list = new ArrayList<>();
		for(Tuple tuple : set){
			GuildScore score = new GuildScore(tuple.getElement());
			score.setAl(new AtomicLong((long)tuple.getScore()));
			list.add(score);
		}
		return list;
	}

	@Override
	public void remove(String element) {
		String key = getKey();
		ActivityLocalRedis.getInstance().zrem(key, element);
	}

	private boolean isExistGuild(String guildId){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.SUPER_POWER_LAB_VALUE);
		if(opActivity.isPresent()){
			PowerCollectActivity activity = (PowerCollectActivity)opActivity.get();
			return activity.getDataGeter().isGuildExist(guildId);
		}
		return true; //默认不乱删
	}
}
