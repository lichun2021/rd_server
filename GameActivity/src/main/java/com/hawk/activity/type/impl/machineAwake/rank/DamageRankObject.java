package com.hawk.activity.type.impl.machineAwake.rank;

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
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeActivityKVCfg;
import com.hawk.activity.type.impl.machineAwake.entity.MachineAwakeEntity;
import com.hawk.game.protocol.Activity.DamageRankInfo;
import com.hawk.game.protocol.Activity.DamageRankType;
import com.hawk.game.protocol.Activity.GetDamageRankInfoResp;

import redis.clients.jedis.Tuple;

public class DamageRankObject {
	
	public DamageRankType rankType;
	
	private List<DamageRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public DamageRankObject(DamageRankType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	public List<DamageRankInfo> getRankInfos() {
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
		case SELF_DAMAGE_RANK:
			key = ActivityRedisKey.MACHINE_AWAKE_DAMAGE_SELF_RANK;
			break;
		case GUILD_DAMAGE_RANK:
			key = ActivityRedisKey.MACHINE_AWAKE_DAMAGE_GUILD_RANK;
			break;
		}
		return key + termId;
	}
	
	/**
	 * 获取榜单数量限制
	 * @return
	 */
	public int getRankLimit() {
		MachineAwakeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeActivityKVCfg.class);
		int limit = 1;
		switch (rankType) {
		case SELF_DAMAGE_RANK:
			limit = cfg.getSelfRankLimit();
			break;
		case GUILD_DAMAGE_RANK:
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
		List<DamageRankInfo> list = new ArrayList<>();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			DamageRankInfo.Builder builder = buildRankInfo(rank, tuple);
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
	private DamageRankInfo.Builder buildRankInfo(int rank, Tuple tuple) {
		DamageRankInfo.Builder builder = DamageRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = "";
		String name = "";
		List<Integer> privateOptionVals = new ArrayList<Integer>();
		switch (rankType) {
		case SELF_DAMAGE_RANK:
			name = dataGeter.getPlayerName(id);
			guildTag = dataGeter.getGuildTagByPlayerId(id);
			privateOptionVals.addAll(dataGeter.getPersonalProtectVals(id));
			break;
		case GUILD_DAMAGE_RANK:
			name = dataGeter.getGuildName(id);
			guildTag = dataGeter.getGuildTag(id);
			break;
		}

		builder.setName(name);
		if (!privateOptionVals.isEmpty()) {
			builder.setPlayerId(id);
			builder.addAllPersonalProtectSwitch(privateOptionVals);
		}
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
	private DamageRankInfo.Builder buildSelfRankInfo(MachineAwakeEntity entity) {
		
		String playerId = entity.getPlayerId();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = dataGeter.getGuildTagByPlayerId(playerId);
		String name = "";
		String rankId = playerId;
		List<Integer> privateOptionVals = new ArrayList<Integer>();
		switch (rankType) {
		case SELF_DAMAGE_RANK:
			name = dataGeter.getPlayerName(playerId);
			privateOptionVals.addAll(dataGeter.getPersonalProtectVals(playerId));
			break;
		case GUILD_DAMAGE_RANK:
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
		
		DamageRankInfo.Builder builder = DamageRankInfo.newBuilder();
		builder.setName(name == null ? "" : name);
		if (!privateOptionVals.isEmpty()) {
			builder.setPlayerId(playerId);
			builder.addAllPersonalProtectSwitch(privateOptionVals);
		}
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
	public GetDamageRankInfoResp.Builder buildRankInfoResp(MachineAwakeEntity entity) {
		GetDamageRankInfoResp.Builder builder = GetDamageRankInfoResp.newBuilder();
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
