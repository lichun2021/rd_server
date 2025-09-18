package com.hawk.activity.type.impl.strongestGuild.rank.impl;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.util.JsonUtils;

import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.cache.PersonData;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;

import redis.clients.jedis.Tuple;

/***
 * 王者联盟个人总排行
 * @author yang.rao
 *
 */
public class StrongestGuildPersonalTotalRank implements StrongestGuildRank {

	//key为playerId-stageId的为阶段积分
	private Map<String, PersonData> scoreMap = new ConcurrentHashMap<>();
	
	private Set<Tuple> cacheRankList = new LinkedHashSet<>();
	
	@Override
	public String key() {
		int termId = getTermId();
		if(termId == 0){
			return null;
		}
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSONAL_TOTAL_RANK, termId);
		return key;
	}

	@Override
	public Set<Tuple> getRankList() {
		return cacheRankList;
	}

	@Override
	public boolean addScore(double score, String member) {
		if(member == null){
			return false;
		}
		int stageId = getStageId();
		if(stageId == 0){
			return false;
		}
		int termId = getTermId();
		PersonData personData = scoreMap.get(member);
		if(personData == null){
			if(termId == 0){
				return false;
			}
			//尝试从redis读取，如果没有，则初始化
			personData = readDataFromRedis(member);
			if(personData == null){
				personData = new PersonData(member, termId);
			}
			scoreMap.put(member, personData);
		}
		personData.addScore(stageId, (long)score);
		long totalScore = personData.calTotalScore();
		ActivityLocalRedis.getInstance().zadd(key(), totalScore, member);
		ActivityLocalRedis.getInstance().hsetWithExpire(getMapDataKey(), member, JsonUtils.Object2Json(personData), StrongestGuildActivity.expireTime);
		return true;
	}

	@Override
	public double getScore(String member) {
		Double score = ActivityLocalRedis.getInstance().zScore(key(), member);
		double s =  score == null ? 0 : score;
		return s;
	}

	@Override
	public void clear() {
		scoreMap.clear();
	}
	
	private PersonData readDataFromRedis(String playerId){
		String msg = ActivityLocalRedis.getInstance().hget(getMapDataKey(), playerId);
		if(msg == null){
			return null;
		}
		PersonData data = JsonUtils.String2Object(msg, PersonData.class);
		int termId = getTermId();
		if(data.getTermId() != termId){
			return null;
		}
		return data;
	}
	
	private String getMapDataKey(){
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSON_TOTAL_SCORE_MAP, getTermId());
		return key;
	}

	@Override
	public void remove(String element) {
		String key = key();
		ActivityLocalRedis.getInstance().zrem(key, element);
	}

	@Override
	public void doRank() {
		String key = key();
		if(key == null){
			HawkLog.errPrintln("StrongestGuildPersonalTotalRank key is null, curStage:{}", getStageId());
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrangeWithExipre(key, 0, Math.max((rankSize - 1), 0), StrongestGuildActivity.expireTime);
		cacheRankList = exchangeSet(set);
	}
}
