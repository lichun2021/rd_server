package com.hawk.activity.type.impl.machineLab.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;

import redis.clients.jedis.Tuple;

public class MachineLabRank{
	/**
	 * 期数
	 */
	private int termId;
	
	/**
	 * 服务器ID
	 */
	private String serverId;
	
	/**
	 * 排行榜刷新时间
	 */
	private long rankLoadTime;
	
	/**
	 * 排行榜缓存
	 */
	private List<MachineLabRankMember> rankMembers;
	
	/**
	 * 过期时间
	 */
	private int expireSeconds;

	
	public MachineLabRank(int termId,String serverId,int expireSeconds) {
		this.serverId = serverId;
		this.termId = termId;
		this.rankLoadTime = 0;
		this.expireSeconds = expireSeconds;
		this.rankMembers = new ArrayList<>();
	}
	
	
	
	

	/**
	 * 加载排行
	 * @param guildId
	 * @return
	 */
	public List<MachineLabRankMember> doRankSort(int size) {
		String redisKey = this.getRedisKey();
		if(HawkOSOperator.isEmptyString(redisKey)){
			return null;
		}
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance()
				.zrevrange(redisKey, 0, size -1);		
		List<MachineLabRankMember> newRankList = new ArrayList<>();
		int index = 1;
		for (Tuple rank : rankSet) {
			ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
			if (dataGeter.getOpenId(rank.getElement()) == null) {
				ActivityGlobalRedis.getInstance().getRedisSession().zRem(redisKey, 0, rank.getElement());
				continue;
			}
			MachineLabRankMember member = new MachineLabRankMember();
			member.setPlayerId(rank.getElement());
			member.setRank(index);
			long score = (long) rank.getScore();
			member.setScore(score);
			newRankList.add(member);
			index++;
		}
		return newRankList;
	}

	

	/**
	 * 设置积分
	 * @param id
	 * @param guildId
	 * @param score
	 */
	public void setScore(String playerId,int score) {
		double rankScore = HawkTime.getMillisecond() * 0.0000000000001;
		rankScore = 1d - rankScore;
		rankScore += score;
		String redisKey = this.getRedisKey();
		ActivityGlobalRedis.getInstance().getRedisSession().zAdd(
				redisKey, rankScore, playerId, this.expireSeconds);
	}


	/**
	 * 获取排行
	 * @param id
	 * @param guildId
	 * @return
	 */
	public MachineLabRankMember getRank(String playerId) {
		String redisKey = this.getRedisKey();
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(redisKey, playerId);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
		}
		MachineLabRankMember member = new MachineLabRankMember();
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
	public List<MachineLabRankMember> getRankShowMembers(int size) {
		long curTime = HawkTime.getMillisecond();
		if(this.rankMembers != null && curTime - rankLoadTime < 5 * HawkTime.MINUTE_MILLI_SECONDS){
			return this.rankMembers;
		}
		List<MachineLabRankMember> newlist = doRankSort(size);
		this.rankMembers = newlist;
		this.rankLoadTime = curTime;
		return newlist;
	}
	
	public void rankMembersReset() {
		this.rankMembers = null;
	}
		
	/**
	 * 获取redisKey
	 * @param guildId
	 * @return
	 */
	private String getRedisKey(){
		String key = "MACHINE_LAB_ASSAULT_RANK:"+ this.serverId+":"+this.termId;
		return key;
	}
	
	
	public void clearAllRank(){
		
	}
	
	public void removeRank(String playerId) {
		String redisKey = this.getRedisKey();
		ActivityLocalRedis.getInstance().zrem(redisKey, playerId);
	}
}
