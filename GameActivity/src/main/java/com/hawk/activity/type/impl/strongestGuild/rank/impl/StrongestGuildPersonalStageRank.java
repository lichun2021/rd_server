package com.hawk.activity.type.impl.strongestGuild.rank.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hawk.log.HawkLog;

import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;

import redis.clients.jedis.Tuple;

/***
 * 王者联盟个人阶段排行榜
 * @author yang.rao
 *
 */
public class StrongestGuildPersonalStageRank implements StrongestGuildRank {

	private Set<Tuple> cacheRankList = new LinkedHashSet<>();
	
	@Override
	public String key() {
		int termId = getTermId();
		int stageId = getStageId();
		if(termId == 0 || stageId == 0){
			return null;
		}
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSONAL_RANK, termId, stageId);
		return key;
	}

	@Override
	public Set<Tuple> getRankList() {
		return cacheRankList;
	}

	@Override
	public boolean addScore(double score, String playerId) {
		ActivityLocalRedis.getInstance().zIncrby(key(), playerId, score);
		return true;
	}

	@Override
	public double getScore(String playerId) {
		Double score = ActivityLocalRedis.getInstance().zScore(key(), playerId);
		double s =  score == null ? 0 : score;
		return s;
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
			HawkLog.errPrintln("StrongestGuildPersonalStageRank key is null, curStage:{}", getStageId());
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrangeWithExipre(key, 0, Math.max((rankSize - 1), 0), StrongestGuildActivity.expireTime);
		Set<Tuple> newSet = exchangeSet(set);
		cacheRankList = newSet;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}
