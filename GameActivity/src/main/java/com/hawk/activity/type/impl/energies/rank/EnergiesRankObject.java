package com.hawk.activity.type.impl.energies.rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.energies.cfg.EnergiesActivityKVCfg;
import com.hawk.activity.type.impl.energies.entity.EnergiesEntity;
import com.hawk.game.protocol.Activity.EnergiesRankInfo;
import com.hawk.game.protocol.Activity.EnergiesRankType;
import com.hawk.game.protocol.Activity.GetEnergiesRankInfoResp;

import redis.clients.jedis.Tuple;

public class EnergiesRankObject {
	
	public EnergiesRankType rankType;
	
	private List<EnergiesRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public EnergiesRankObject(EnergiesRankType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	public List<EnergiesRankInfo> getRankInfos() {
		return rankInfos;
	}

	public Set<Tuple> getRankTuples() {
		return rankTuples;
	}
	
	/**
	 * 获取排行key值
	 * @param termId
	 * @return
	 */
	String getRankKey(int termId) {
		String key = "";
		switch (rankType) {
		case ENERGIES_SELF_RANK:
			key = ActivityRedisKey.ENERGIES_SELF_RANK;
			break;
		case ENERGIES_GUILD_RANK:
			key = ActivityRedisKey.ENERGIES_GUILD_RANK;
			break;
		}
		return key + ":" + termId;
	}
	
	/**
	 * 获取榜单数量限制
	 * @return
	 */
	public int getRankLimit() {
		EnergiesActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EnergiesActivityKVCfg.class);
		int limit = 1;
		switch (rankType) {
		case ENERGIES_SELF_RANK:
			limit = cfg.getPersonRankSize();
			break;
		case ENERGIES_GUILD_RANK:
			limit = cfg.getGuildRankSize();
			break;
		}
		return limit;
	}
	
	/**
	 * 添加排行积分
	 * @param id
	 * @param termId
	 * @param addScore
	 */
	public long addRankScore(String id, int termId, int addScore) {
		if (HawkOSOperator.isEmptyString(id)) {
			return 0;
		}
		String key = getRankKey(termId);
		return ActivityLocalRedis.getInstance().zIncrby(key, id, addScore).longValue();
	}
	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank(int termId) {
		int rankSize = getRankLimit();
		if (rankSize > 0) {
			rankSize -= 1;
		}
		String key = getRankKey(termId);
		this.rankTuples = ActivityLocalRedis.getInstance().zrevrange(key, 0, rankSize);
		List<EnergiesRankInfo> list = new ArrayList<>();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			EnergiesRankInfo.Builder builder = buildRankInfo(rank, tuple);
			list.add(builder.build());
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

	/**
	 * 构建排行单元信息
	 * @param rank
	 * @param tuple
	 * @return
	 */
	private EnergiesRankInfo.Builder buildRankInfo(int rank, Tuple tuple) {
		EnergiesRankInfo.Builder builder = EnergiesRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = "";
		String name = "";
		switch (rankType) {
		case ENERGIES_SELF_RANK:
			name = dataGeter.getPlayerName(id);
			guildTag = dataGeter.getGuildTagByPlayerId(id);
			break;
		case ENERGIES_GUILD_RANK:
			name = dataGeter.getGuildName(id);
			guildTag = dataGeter.getGuildTag(id);
			builder.setGuildFlag(dataGeter.getGuildFlag(id));
			break;
		}

		builder.setName(name);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		builder.setScore(score);
		builder.setRank(rank);
		return builder;
	}
	
	/**
	 * 构建自己的排行信息
	 * @param entity
	 * @return
	 */
	private EnergiesRankInfo.Builder buildSelfRankInfo(EnergiesEntity entity) {
		
		String playerId = entity.getPlayerId();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = dataGeter.getGuildTagByPlayerId(playerId);
		String name = "";
		String rankId = playerId;
		int guildFlag = 0;
		switch (rankType) {
		case ENERGIES_SELF_RANK:
			name = dataGeter.getPlayerName(playerId);
			break;
		case ENERGIES_GUILD_RANK:
			name = dataGeter.getGuildNameByByPlayerId(playerId);
			rankId = dataGeter.getGuildId(playerId);
			guildFlag = dataGeter.getGuildFlag(rankId);
			break;
		}
	
		String key = getRankKey(entity.getTermId());
		RedisIndex index = null;
		if (rankId != null) {
			index = ActivityLocalRedis.getInstance().zrank(key, rankId);
		}
		long selfScore = (long) (index == null ? 0 : index.getScore());
		int rank = getSelfRank(rankId);
		
		EnergiesRankInfo.Builder builder = EnergiesRankInfo.newBuilder();
		builder.setName(name == null ? "" : name);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		builder.setScore(selfScore);
		builder.setRank(rank);
		builder.setGuildFlag(guildFlag);
		return builder;
	}
	
	/**
	 * 获取自己/本联盟排名
	 * @param rankId
	 * @return
	 */
	private int getSelfRank(String rankId){
		int selfRank = -1;
		int rank = 1;
		for(Tuple tuple : this.rankTuples){
			if(tuple.getElement().equals(rankId)){
				selfRank = rank;
				break;
			}
			rank ++;
		}
		return selfRank;
	}
	
	/**
	 * 构建排行榜返回信息
	 * @param entity
	 * @return
	 */
	public GetEnergiesRankInfoResp.Builder buildRankInfoResp(EnergiesEntity entity) {
		GetEnergiesRankInfoResp.Builder builder = GetEnergiesRankInfoResp.newBuilder();
		builder.addAllRankInfo(this.rankInfos);
		builder.setSelfRank(buildSelfRankInfo(entity));
		builder.setRankType(rankType);
		return builder;
	}
	
	/**
	 * 移除指定成员排行数据
	 * @param guildId
	 * @param activityTermId
	 */
	public void removeRank(String id, int termId) {
		String key = getRankKey(termId);
		ActivityLocalRedis.getInstance().zrem(key, id);
	}
	
}
