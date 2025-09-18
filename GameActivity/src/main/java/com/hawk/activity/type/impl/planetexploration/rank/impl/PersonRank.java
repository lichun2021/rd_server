package com.hawk.activity.type.impl.planetexploration.rank.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreActivity;
import com.hawk.activity.type.impl.planetexploration.cache.PersonScore;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.activity.type.impl.planetexploration.rank.PlanetExploreRank;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;

public class PersonRank implements PlanetExploreRank<PersonScore> {

	private List<PersonScore> rankList = new ArrayList<>();
	
	@Override
	public String getKey() {
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_SCORE_RANK, termId);
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
			long score = RankScoreHelper.getRealScore((long)t.getScore());
			personData.setScore(score);
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
		long rankScore = RankScoreHelper.calcSpecialRankScore((long)score);
		ActivityLocalRedis.getInstance().zaddWithExpire(getKey(), rankScore, playerId, PlanetExploreActivity.expireTime);
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
		PlanetExploreKVCfg config = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		return config.getPersonRankSize();
	}

	@Override
	public List<PersonScore> getHasRewardRankList(int termId, int maxSize) {
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_SCORE_RANK, termId);
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((rankSize - 1), 0));
		List<PersonScore> list = new ArrayList<>();
		for(Tuple tuple : set){
			PersonScore score = new PersonScore(tuple.getElement());
			long rankScore = RankScoreHelper.getRealScore((long)tuple.getScore());
			score.setScore(rankScore);
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
	}
}
