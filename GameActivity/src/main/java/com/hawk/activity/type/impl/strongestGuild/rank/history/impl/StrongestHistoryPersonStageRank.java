package com.hawk.activity.type.impl.strongestGuild.rank.history.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.strongestGuild.rank.history.StrongestHistoryRank;

import redis.clients.jedis.Tuple;

public class StrongestHistoryPersonStageRank implements StrongestHistoryRank {

	private int termId;
	
	private int stageId;
	
	public StrongestHistoryPersonStageRank(int termId, int stageId){
		this.termId = termId;
		this.stageId = stageId;
	}
	
	@Override
	public Set<String> getHistoryRank(int high, int expireSeconds) {
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSONAL_RANK, termId, stageId);
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrangeWithExipre(key, 0, high, expireSeconds);
		Set<String> reSet = new LinkedHashSet<>();
		for(Tuple t : set){
			reSet.add(t.getElement());
		}
		return reSet;
	}

	@Override
	public String toString() {
		return "StrongestHistoryPersonStageRank [termId=" + termId + ", stageId=" + stageId + "]";
	}
}
