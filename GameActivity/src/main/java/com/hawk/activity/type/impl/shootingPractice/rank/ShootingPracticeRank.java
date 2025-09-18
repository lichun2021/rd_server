package com.hawk.activity.type.impl.shootingPractice.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.shootingPractice.ShootingPracticeActivity;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeKVCfg;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.PBShootingPracticeScoreRank;
import com.hawk.gamelib.rank.RankScoreHelper;

import redis.clients.jedis.Tuple;

public class ShootingPracticeRank{

	private List<PBShootingPracticeScoreRank> rankList = new ArrayList<>();
	
	public String getKey() {
		int termId = getTermId();
		String key = String.format(ActivityRedisKey.SHOOTING_PRACTICE_SCORE_RANK, termId);
		return key;
	}

	public void doRankSort() {
		String key = getKey();
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((rankSize - 1), 0));
		List<PBShootingPracticeScoreRank> rankListTemp = new ArrayList<>(rankSize);
		int rank = 1;
		for(Tuple t : set){
			String playerId = t.getElement();
			ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
			if (dataGeter.getOpenId(playerId) == null) {
				this.delPlayerScore(playerId);
				continue;
			}
			long score = RankScoreHelper.getRealScore((long)t.getScore());
			PBShootingPracticeScoreRank.Builder rBuilder = PBShootingPracticeScoreRank.newBuilder();
			rBuilder.setPlayerId(playerId);
			rBuilder.setScore(score);
			rBuilder.setRank(rank);
			rankListTemp.add(rBuilder.build());
			rank ++;
		}
		this.rankList = rankListTemp;
	}

	public boolean addScore(int score, String member) {
		if(member == null || score == 0){
			return false;
		}
		int termId = getTermId();
		if(termId == 0){
			return false;
		}
		String playerId = member;
		long rankScore = RankScoreHelper.calcSpecialRankScore(score);
		ActivityLocalRedis.getInstance().zaddWithExpire(getKey(), rankScore, playerId, (int)TimeUnit.DAYS.toSeconds(30));
		return true;
	}

	public void delPlayerScore(String member){
		ActivityLocalRedis.getInstance().zrem(getKey(), member);
	}
	

	public PBShootingPracticeScoreRank getPlayerCurRank(String member){
		if(HawkOSOperator.isEmptyString(member)){
			return null;
		}
		int termId = getTermId();
		if(termId == 0){
			return null;
		}
		
		String key = this.getKey();
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, member);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore().longValue();
			score = RankScoreHelper.getRealScore(score);
		}
		PBShootingPracticeScoreRank.Builder rBuilder = PBShootingPracticeScoreRank.newBuilder();
		rBuilder.setRank(rank);
		rBuilder.setScore(score);
		rBuilder.setPlayerId(member);
		return rBuilder.build();
	}
	

	public List<PBShootingPracticeScoreRank> getRankList() {
		return rankList;
	}

	
	
	public int getRankSize() {
		ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
		return config.getRankSize();
	}
	
	


	
	private int getTermId(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.SHOOTING_PRACTICE_VALUE);
		if(opActivity.isPresent()){
			ShootingPracticeActivity activity = (ShootingPracticeActivity)opActivity.get();
			return activity.getActivityTermId();
		}
		return 0;
	}
	
	public List<PBShootingPracticeScoreRank> getHasRewardRankList(int termId, int maxSize) {
		String key = String.format(ActivityRedisKey.SHOOTING_PRACTICE_SCORE_RANK, termId);
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrange(key, 0, Math.max((maxSize - 1), 0));
		List<PBShootingPracticeScoreRank> rlist = new ArrayList<>();
		int rank = 1;
		for(Tuple t : set){
			String playerId = t.getElement();
			long score = RankScoreHelper.getRealScore((long)t.getScore());
			PBShootingPracticeScoreRank.Builder rBuilder = PBShootingPracticeScoreRank.newBuilder();
			rBuilder.setPlayerId(playerId);
			rBuilder.setScore(score);
			rBuilder.setRank(rank);
			rlist.add(rBuilder.build());
			rank++;
		}
		return rlist;
	}

}
