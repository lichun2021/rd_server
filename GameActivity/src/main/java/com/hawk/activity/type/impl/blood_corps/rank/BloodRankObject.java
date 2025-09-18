package com.hawk.activity.type.impl.blood_corps.rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsActivityKVCfg;
import com.hawk.activity.type.impl.blood_corps.entity.BloodCorpsEntity;
import com.hawk.game.protocol.Activity.BloodRankInfo;
import com.hawk.game.protocol.Activity.BloodRankListResp;

import redis.clients.jedis.Tuple;

public class BloodRankObject {
	private List<BloodRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public BloodRankObject() {
		super();
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank(int termId) {
		BloodCorpsActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BloodCorpsActivityKVCfg.class);
		int rankSize = cfg.getRankLimit();
		if (rankSize > 0) {
			rankSize -= 1;
		}
		this.rankTuples = ActivityLocalRedis.getInstance().zrevrange(ActivityRedisKey.BLOOD_CORPS_RANK + termId, 0, rankSize);
		List<BloodRankInfo> list = new ArrayList<>();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			try {
				String playerId = tuple.getElement();
				long score = (long) tuple.getScore();
				BloodRankInfo.Builder builder = BloodRankInfo.newBuilder();
				builder.setName(dataGeter.getPlayerName(playerId));
				builder.setPlayerId(playerId);
				builder.addAllPersonalProtectSwitch(dataGeter.getPersonalProtectVals(playerId));
				builder.setScore(score);
				builder.setRank(rank);
				list.add(builder.build());
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			rank++;
		}
		this.rankInfos = list;
	}
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(int termId){
		this.rankInfos = new ArrayList<>();
		this.rankTuples = new HashSet<>();
		ActivityLocalRedis.getInstance().del(ActivityRedisKey.BLOOD_CORPS_RANK + termId);
	}
	
	public List<BloodRankInfo> getRankInfos() {
		return rankInfos;
	}

	public Set<Tuple> getRankTuples() {
		return rankTuples;
	}
	
	private int getSelfRank(String playerId){
		int selfRank = -1;
		int rank = 1;
		for(Tuple tuple : this.rankTuples){
			if(tuple.getElement().equals(playerId)){
				selfRank = rank;
				break;
			}
			rank ++;
		}
		return selfRank;
	}
	
	public BloodRankListResp.Builder buildRankInfoList(BloodCorpsEntity entity) {
		String playerId = entity.getPlayerId();
		BloodRankListResp.Builder builder = BloodRankListResp.newBuilder();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String playerName = dataGeter.getPlayerName(playerId);
		BloodRankInfo.Builder selfRank = BloodRankInfo.newBuilder();
		selfRank.setName(playerName);
		selfRank.setPlayerId(playerId);
		selfRank.addAllPersonalProtectSwitch(dataGeter.getPersonalProtectVals(playerId));
		selfRank.setRank(getSelfRank(playerId));
		RedisIndex index = ActivityLocalRedis.getInstance().zrank(ActivityRedisKey.BLOOD_CORPS_RANK + entity.getTermId(), playerId);
		long score = index == null ? 0 : index.getScore().longValue();
		selfRank.setScore(score);
		builder.setSelfRank(selfRank);
		builder.addAllRankInfo(this.rankInfos);
		builder.setScoreInfo(entity.genScoreInfo());
		return builder;
	}
	
	/**
	 * 移除玩家并且刷新排行榜,慎用(需要重新刷排行榜)
	 * @param termId
	 * @param playerId
	 */
	public void removeRank(int termId, String playerId) {
		String redisKey = ActivityRedisKey.BLOOD_CORPS_RANK + termId;
		ActivityLocalRedis.getInstance().zrem(redisKey, playerId);
		refreshRank(termId);
	}
}
