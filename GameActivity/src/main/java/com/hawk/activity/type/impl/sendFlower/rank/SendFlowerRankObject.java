package com.hawk.activity.type.impl.sendFlower.rank;

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
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerActivityKVCfg;
import com.hawk.activity.type.impl.sendFlower.entity.SendFlowerEntity;
import com.hawk.game.protocol.Activity.PBSendFlowerRankInfo;
import com.hawk.game.protocol.Activity.PBSendFlowerRankInfoResp;
import com.hawk.game.protocol.Activity.PBSendFlowerType;

import redis.clients.jedis.Tuple;

public class SendFlowerRankObject {

	public PBSendFlowerType rankType;

	private List<PBSendFlowerRankInfo> rankInfos;

	private Set<Tuple> rankTuples;

	public SendFlowerRankObject(PBSendFlowerType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();
		;
		this.rankTuples = new HashSet<Tuple>();
	}

	public List<PBSendFlowerRankInfo> getRankInfos() {
		return rankInfos;
	}

	public Set<Tuple> getRankTuples() {
		return rankTuples;
	}

	/** 获取排行key值
	 * 
	 * @param termId
	 * @return */
	String getRankKey(int termId) {
		String key = "";
		switch (rankType) {
		case SONG_HUA_TYPE:
			key = ActivityRedisKey.SONG_HUA_RANK;
			break;
		case SHOU_HUA_TYPE:
			key = ActivityRedisKey.SHOU_HUA_RANK;
			break;
		}
		return key + termId;
	}

	/** 获取榜单数量限制
	 * 
	 * @return */
	public int getRankLimit() {
		SendFlowerActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SendFlowerActivityKVCfg.class);
		return cfg.getSelfRankLimit();
	}

	/** 添加排行积分
	 * 
	 * @param playerId
	 * @param termId
	 * @param addScore */
	public void addRankScore(String playerId, int termId, int addScore) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		String key = getRankKey(termId);
		ActivityLocalRedis.getInstance().zIncrby(key, playerId, addScore);
	}

	/** 刷新排行列表 */
	public void refreshRank(int termId) {
		int rankSize = getRankLimit();
		if (rankSize > 0) {
			rankSize -= 1;
		}
		String key = getRankKey(termId);
		this.rankTuples = ActivityLocalRedis.getInstance().zrevrange(key, 0, rankSize);
		List<PBSendFlowerRankInfo> list = new ArrayList<>();
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			PBSendFlowerRankInfo.Builder builder = buildRankInfo(rank, tuple);
			list.add(builder.build());
			rank++;
		}
		this.rankInfos = list;
	}

	/** 清除排行信息 */
	public void clearRank(int termId) {
		this.rankInfos = new ArrayList<>();
		this.rankTuples = new HashSet<>();
		ActivityLocalRedis.getInstance().del(ActivityRedisKey.BLOOD_CORPS_RANK + termId);
	}

	/** 构建排行单元信息
	 * 
	 * @param rank
	 * @param tuple
	 * @return */
	private PBSendFlowerRankInfo.Builder buildRankInfo(int rank, Tuple tuple) {
		PBSendFlowerRankInfo.Builder builder = PBSendFlowerRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String name = dataGeter.getPlayerName(id);
		String guildTag = dataGeter.getGuildTagByPlayerId(id);

		builder.setName(name);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		builder.setScore(score);
		builder.setRank(rank);
		return builder;
	}

	/** 构建自己的排行信息
	 * 
	 * @param entity
	 * @return */
	private PBSendFlowerRankInfo.Builder buildSelfRankInfo(SendFlowerEntity entity) {

		String playerId = entity.getPlayerId();
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String guildTag = dataGeter.getGuildTagByPlayerId(playerId);
		String name = dataGeter.getPlayerName(playerId);
		String rankId = playerId;

		String key = getRankKey(entity.getTermId());
		RedisIndex index = ActivityLocalRedis.getInstance().zrank(key, rankId);
		long selfScore = (long) (index == null ? 0 : index.getScore());
		int rank = getSelfRank(rankId);

		PBSendFlowerRankInfo.Builder builder = PBSendFlowerRankInfo.newBuilder();
		builder.setName(name == null ? "" : name);
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		builder.setScore(selfScore);
		builder.setRank(rank);
		return builder;
	}

	/** 获取自己/本联盟排名
	 * 
	 * @param rankId
	 * @return */
	private int getSelfRank(String rankId) {
		int selfRank = -1;
		int rank = 1;
		for (Tuple tuple : this.rankTuples) {
			if (tuple.getElement().equals(rankId)) {
				selfRank = rank;
				break;
			}
			rank++;
		}
		return selfRank;
	}

	/** 构建排行榜返回信息
	 * 
	 * @param entity
	 * @return */
	public PBSendFlowerRankInfoResp.Builder buildRankInfoResp(SendFlowerEntity entity) {
		PBSendFlowerRankInfoResp.Builder builder = PBSendFlowerRankInfoResp.newBuilder();
		builder.addAllRankInfo(this.rankInfos);
		builder.setSelfRank(buildSelfRankInfo(entity));
		builder.setRankType(rankType);
		return builder;
	}

	/** 移除指定成员排行数据
	 * 
	 * @param guildId
	 * @param activityTermId */
	public void removeRank(String id, int termId) {
		String key = getRankKey(termId);
		ActivityLocalRedis.getInstance().zrem(key, id);
	}

}
