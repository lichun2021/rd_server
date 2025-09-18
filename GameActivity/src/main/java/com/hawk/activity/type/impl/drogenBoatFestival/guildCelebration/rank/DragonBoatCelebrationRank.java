package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.DragonBoatCelebrationActivity;

import redis.clients.jedis.Tuple;

public class DragonBoatCelebrationRank{
	
	/**
	 * 排行榜刷新时间
	 */
	private static Map<String,Long> rankLoadTimeMap = new ConcurrentHashMap<>();
	
	/**
	 * 排行榜缓存
	 */
	private static Map<String,List<DragonBoatCelebrationRankMember>> rankMap = new ConcurrentHashMap<>();
	

	

	/**
	 * 加载排行
	 * @param guildId
	 * @return
	 */
	public static List<DragonBoatCelebrationRankMember> doRankSort(String guildId) {
		Optional<DragonBoatCelebrationActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return null;
		}
		String guildRankKey = getRedisKey(guildId);
		if(HawkOSOperator.isEmptyString(guildRankKey)){
			return null;
		}
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(guildId), 0, Integer.MAX_VALUE);		
		List<DragonBoatCelebrationRankMember> newRankList = new ArrayList<>();
		int index = 1;
		for (Tuple rank : rankSet) {
			DragonBoatCelebrationRankMember member = new DragonBoatCelebrationRankMember();
			member.setPlayerId(rank.getElement());
			member.setRank(index);
			long score = (long) rank.getScore();
			member.setScore(score);
			newRankList.add(member);
			index++;
		}
		long curTime = HawkTime.getMillisecond();
		rankMap.put(guildId, newRankList);
		rankLoadTimeMap.put(guildId, curTime);
		return newRankList;
	}

	

	/**
	 * 添加积分
	 * @param id
	 * @param guildId
	 * @param score
	 */
	public static void addScore(String playerId, String guildId,int score) {
		ActivityGlobalRedis.getInstance().getRedisSession().zIncrby(
				getRedisKey(guildId), playerId, score, (int)TimeUnit.DAYS.toSeconds(30));
	}


	/**
	 * 获取排行
	 * @param id
	 * @param guildId
	 * @return
	 */
	public static DragonBoatCelebrationRankMember getRank(String playerId,String guildId) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(guildId), playerId);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		DragonBoatCelebrationRankMember member = new DragonBoatCelebrationRankMember();
		member.setPlayerId(playerId);
		member.setRank(rank);
		member.setScore(score);
		return member;
	}
	
	
	/**
	 * 获取排行榜人员
	 * @param guidlId
	 * @return
	 */
	public static List<DragonBoatCelebrationRankMember> getRankMembers(String guidlId) {
		List<DragonBoatCelebrationRankMember> list = rankMap.get(guidlId);
		long curTime = HawkTime.getMillisecond();
		long rankLoadTime = 0;
		if(rankLoadTimeMap.containsKey(guidlId)){
			rankLoadTime = rankLoadTimeMap.get(guidlId);
		}
		if(list != null && curTime - rankLoadTime < 5 * HawkTime.MINUTE_MILLI_SECONDS){
			return list;
		}
		List<DragonBoatCelebrationRankMember> newlist = doRankSort(guidlId);
		rankMap.put(guidlId, newlist);
		rankLoadTimeMap.put(guidlId, curTime);
		return newlist;
	}
		
	/**
	 * 获取redisKey
	 * @param guildId
	 * @return
	 */
	private static String getRedisKey(String guildId){
		Optional<DragonBoatCelebrationActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			DragonBoatCelebrationActivity activity = opActivity.get();
			return activity.getGuildRankKey(guildId);
		} else {
			return null;
		}
		
	}
	
	
	public static void clearAllRank(){
		rankMap.clear();
		rankLoadTimeMap.clear();
	}
}
