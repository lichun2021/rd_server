package com.hawk.activity.type.impl.snowball.rank;

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
import com.hawk.activity.type.impl.snowball.cfg.SnowballCfg;
import com.hawk.activity.type.impl.snowball.entity.SnowballEntity;
import com.hawk.game.protocol.Activity.SnowballRankInfo;
import com.hawk.game.protocol.Activity.SnowballRankResp;
import com.hawk.game.protocol.Activity.SnowballRankType;

import redis.clients.jedis.Tuple;

/**
 * 雪球大战排行榜
 * @author golden
 *
 */
public class SnowballRankObject {
	
	public SnowballRankType rankType;
	
	private List<SnowballRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public SnowballRankObject(SnowballRankType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	public List<SnowballRankInfo> getRankInfos() {
		return rankInfos;
	}

	public Set<Tuple> getRankTuples() {
		return rankTuples;
	}
	
	/**
	 * 获取排行key值
	 */
	 String getRankKey(int termId){
		String key = "";
		switch (rankType) {
		case SELF_SNOWBALL_RANK:
			key = ActivityRedisKey.SNOWBALL_SELF_RANK;
			break;
		case GUILD_SNOWBALL_RANK:
			key = ActivityRedisKey.SNOWBALL_GUILD_RANK;
			break;
		}
		return key + termId;
	}
	
	/**
	 * 获取榜单数量限制
	 */
	public int getRankLimit() {
		SnowballCfg cfg = HawkConfigManager.getInstance().getKVInstance(SnowballCfg.class);
		int limit = 1;
		switch (rankType) {
		case SELF_SNOWBALL_RANK:
			limit = cfg.getSelfRankLimit();
			break;
		case GUILD_SNOWBALL_RANK:
			limit = cfg.getGuildRankLimit();
			break;
		}
		return limit;
	}
	
	/**
	 * 添加排行积分
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
		List<SnowballRankInfo> list = new ArrayList<>();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			SnowballRankInfo.Builder builder = buildRankInfo(rank, tuple);
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
	 */
	private SnowballRankInfo.Builder buildRankInfo(int rank, Tuple tuple) {
		SnowballRankInfo.Builder builder = SnowballRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = "";
		String name = "";
		switch (rankType) {
		case SELF_SNOWBALL_RANK:
			name = dataGeter.getPlayerName(id);
			guildTag = dataGeter.getGuildTagByPlayerId(id);
			break;
		case GUILD_SNOWBALL_RANK:
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
	 */
	private SnowballRankInfo.Builder buildSelfRankInfo(SnowballEntity entity) {
		
		String playerId = entity.getPlayerId();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = dataGeter.getGuildTagByPlayerId(playerId);
		String name = "";
		String rankId = playerId;
		switch (rankType) {
		case SELF_SNOWBALL_RANK:
			name = dataGeter.getPlayerName(playerId);
			break;
		case GUILD_SNOWBALL_RANK:
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
		
		SnowballRankInfo.Builder builder = SnowballRankInfo.newBuilder();
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
	 */
	public SnowballRankResp.Builder buildRankInfoResp(SnowballEntity entity) {
		SnowballRankResp.Builder builder = SnowballRankResp.newBuilder();
		builder.addAllRankInfo(this.rankInfos);
		builder.setSelfRank(buildSelfRankInfo(entity));
		builder.setRankType(rankType);
		return builder;
	}
	
	/**
	 * 移除指定成员排行数据
	 */
	public void removeRank(String id, int termId) {
		String key = getRankKey(termId);
		ActivityLocalRedis.getInstance().zrem(key, id);
	}
	
}
