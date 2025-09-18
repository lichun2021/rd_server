package com.hawk.activity.type.impl.christmaswar.rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarKVCfg;
import com.hawk.activity.type.impl.christmaswar.entity.ActivityChristmasWarEntity;
import com.hawk.game.protocol.Activity.ChristmasWarRankInfo;
import com.hawk.game.protocol.Activity.ChristmasWarRankInfoResp;
import com.hawk.game.protocol.Activity.ChristmasWarRankType;

import redis.clients.jedis.Tuple;

public class ChristmasWarRankObject {
	
	private ChristmasWarRankType rankType;
	
	private List<ChristmasWarRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public ChristmasWarRankObject(ChristmasWarRankType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	public List<ChristmasWarRankInfo> getRankInfos() {
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
	 String getRankKey(int termId){
		String key = "";
		switch (rankType) {
		case PERSONAL_DAMAGE_RANK:
			key = ActivityRedisKey.CHRISTMAS_WAR_PERSONAL_DAMAGE;
			break;
		case ALLIANCE_DAMAGE_RANK:
			key = ActivityRedisKey.CHRISTMAS_WAR_GUILD_DAMAGE;
			break;
		case PERSONAL_KILL_RANK:
			key = ActivityRedisKey.CHRISTMAS_WAR_KILL_RANK;
			break;
		}
		return key +":" + termId;
	}
	
	/**
	 * 获取榜单数量限制
	 * @return
	 */
	public int getRankLimit() {
		ChristmasWarKVCfg cfg = ChristmasWarKVCfg.getInstance();
		int limit = 1;
		switch (rankType) {
		case PERSONAL_KILL_RANK:
			limit = cfg.getKillRankLimit();
			break;
		case PERSONAL_DAMAGE_RANK:
			limit = cfg.getSelfRankLimit();
			break;
		case ALLIANCE_DAMAGE_RANK:
			limit = cfg.getGuildRankLimit();
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
		int expireTime = 15 * 86400;
		String key = getRankKey(termId);
		return ActivityLocalRedis.getInstance().zIncrbyWithExpire(key, id, addScore, expireTime).longValue();
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
		List<ChristmasWarRankInfo> list = new ArrayList<>();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			ChristmasWarRankInfo.Builder builder = buildRankInfo(rank, tuple);
			list.add(builder.build());
			rank++;
		}
		this.rankInfos = list;
	}
	
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(int termId) {
		this.rankInfos = new ArrayList<>();
		this.rankTuples = new HashSet<>();
		ActivityLocalRedis.getInstance().del(this.getRankKey(termId));
	}

	/**
	 * 构建排行单元信息
	 * @param rank
	 * @param tuple
	 * @return
	 */
	private ChristmasWarRankInfo.Builder buildRankInfo(int rank, Tuple tuple) {
		ChristmasWarRankInfo.Builder builder = ChristmasWarRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = "";
		String name = "";
		switch (rankType) {
		case PERSONAL_DAMAGE_RANK:
		case PERSONAL_KILL_RANK:
			name = dataGeter.getPlayerName(id);
			guildTag = dataGeter.getGuildTagByPlayerId(id);
			break;
		case ALLIANCE_DAMAGE_RANK:
			name = dataGeter.getGuildName(id);
			guildTag = dataGeter.getGuildTag(id);
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
	private ChristmasWarRankInfo.Builder buildSelfRankInfo(ActivityChristmasWarEntity  entity) {
		String playerId = entity.getPlayerId();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = dataGeter.getGuildTagByPlayerId(playerId);
		String name = "";
		String rankId = playerId;
		switch (rankType) {
		case PERSONAL_DAMAGE_RANK:
		case PERSONAL_KILL_RANK:
			name = dataGeter.getPlayerName(playerId);
			break;
		case ALLIANCE_DAMAGE_RANK:
			name = dataGeter.getGuildNameByByPlayerId(playerId);
			rankId = dataGeter.getGuildId(playerId);
			break;
		}
	
		String key = getRankKey(entity.getTermId());
		RedisIndex index = null;
		if (rankId != null) {
			index = ActivityLocalRedis.getInstance().zrank(key, rankId);
		}
		long selfScore = (long) (index == null ? 0 : index.getScore());
		int rank = getSelfRank(rankId);
		
		ChristmasWarRankInfo.Builder builder = ChristmasWarRankInfo.newBuilder();
		builder.setName(name == null ? "" : name);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		builder.setScore(selfScore);
		builder.setRank(rank);
		
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
	public ChristmasWarRankInfoResp.Builder buildRankInfoResp(ActivityChristmasWarEntity entity) {
		ChristmasWarRankInfoResp.Builder builder = ChristmasWarRankInfoResp.newBuilder();
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

	public ChristmasWarRankType getRankType() {
		return rankType;
	}
	
}
