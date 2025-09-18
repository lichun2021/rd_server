package com.hawk.activity.type.impl.powercollect.rank.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.powercollect.PowerCollectActivity;
import com.hawk.activity.type.impl.powercollect.cache.PersonScore;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectKVCfg;
import com.hawk.activity.type.impl.powercollect.rank.PowerCollectRank;
import redis.clients.jedis.Tuple;

public class PersonRank implements PowerCollectRank<PersonScore> {

	private List<PersonScore> rankList = new ArrayList<>();
	
	@Override
	public String getKey() {
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.POWER_COLLECT_PERSON_RANK, termId);
		return key;
	}

	@Override
	public void doRankSort() {
		String key = getKey();
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((rankSize - 1), 0));
		List<PersonScore> rankList = new ArrayList<>(rankSize);
		for(Tuple t : set){
			PersonScore personData = new PersonScore(t.getElement());
			personData.setScore(t.getScore());
			rankList.add(personData);
		}
		this.rankList = rankList;
	}

	@Override
	public boolean addScore(double score, String member) {
		if(member == null || score == 0){
			return false;
		}
		int termId = getTermId();
		if(termId == 0){
			return false;
		}
		String playerId = member;
		ActivityLocalRedis.getInstance().zIncrbyWithExpire(getKey(), playerId, score, PowerCollectActivity.expireTime);
		return true;
	}

	@Override
	public void clear() {
	}

	@Override
	public PersonScore loadRankData(String playerId) {
		return null;
	}

	@Override
	public List<PersonScore> getRankList() {
		return rankList;
	}

	@Override
	public int getRankSize() {
		PowerCollectKVCfg config = HawkConfigManager.getInstance().getKVInstance(PowerCollectKVCfg.class);
		return config.getPersonRankSize();
	}

	@Override
	public List<PersonScore> getHasRewardRankList(int termId, int maxSize) {
		String key = String.format(ActivityRedisKey.POWER_COLLECT_PERSON_RANK, termId);
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((rankSize - 1), 0));
		List<PersonScore> list = new ArrayList<>();
		for(Tuple tuple : set){
			PersonScore score = new PersonScore(tuple.getElement());
			score.setScore(tuple.getScore());
			list.add(score);
		}
		return list;
	}

	@Override
	public void remove(String playerId) {
		int termId = getTermId();
		if(termId == 0){
			return;
		}
		ActivityLocalRedis.getInstance().zrem(getKey(), playerId);
		doRankSort();
	}
}
