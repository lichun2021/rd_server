package com.hawk.activity.type.impl.strongestGuild.rank.impl;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.util.JsonUtils;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.cache.GuildData;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

/***
 * 联盟总排行
 * 
 * @author yang.rao
 *
 */

public class StrongestGuildTotalGuildRank implements StrongestGuildRank {

	private ConcurrentHashMap<String, GuildData> scoreMap = new ConcurrentHashMap<String, GuildData>();

	/** 联盟排行总榜缓存 **/
	private Set<Tuple> cacheRankList = new LinkedHashSet<>();

	@Override
	public String key() {
		int termId = getTermId();
		if (termId == 0) {
			return null;
		}
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_TOTAL_RANK, termId);
		return key;
	}

	@Override
	public Set<Tuple> getRankList() {
		return cacheRankList;
	}

	@Override
	public boolean addScore(double score, String guildId) {
		if (!isExistGuild(guildId)) {
			return false;
		}
		int stageId = getStageId();
		if (stageId == 0 || guildId == null) {
			return false;
		}
		GuildData data = scoreMap.get(guildId);
		if (data == null) {
			int termId = getTermId();
			if (termId == 0) {
				return false;
			}
			data = createGuildData(guildId, termId, stageId);
		}
		data.addScore(stageId, (long) score);
		long totalScore = data.calTotalScore();
		ActivityLocalRedis.getInstance().zadd(key(), totalScore, guildId);
		String value = JsonUtils.Object2Json(data);
		ActivityLocalRedis.getInstance().hsetWithExpire(getMapDataKey(), guildId, value, StrongestGuildActivity.expireTime);
		return true;
	}

	private synchronized GuildData createGuildData(String guildId, int termId, int stageId) {
		GuildData data = scoreMap.get(guildId);
		if (data != null) {
			return data;
		}
		synchronized (this) {
			data = readDataFromRedis(guildId);
			if (data == null) {
				data = new GuildData(termId, guildId);
			}
			scoreMap.put(guildId, data);
			return data;
		}
	}

	@Override
	public double getScore(String guildId) {
		Double score = ActivityLocalRedis.getInstance().zScore(key(), guildId);
		double s = score == null ? 0 : score;
		return s;
	}

	@Override
	public void clear() {
		scoreMap.clear();
	}

	private GuildData readDataFromRedis(String guildId) {
		String msg = ActivityLocalRedis.getInstance().hget(getMapDataKey(), guildId);
		if (msg == null) {
			return null;
		}
		GuildData data = JsonUtils.String2Object(msg, GuildData.class);
		int termId = getTermId();
		if (data.getTermId() != termId) {
			return null;
		}
		return data;
	}

	private String getMapDataKey() {
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_GUILD_TOTAL_SCORE_MAP, getTermId());
		return key;
	}

	private boolean isExistGuild(String guildId) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.STRONGEST_GUILD_VALUE);
		if (opActivity.isPresent()) {
			StrongestGuildActivity activity = (StrongestGuildActivity) opActivity.get();
			return activity.getDataGeter().isGuildExist(guildId);
		}
		return true; // 默认不乱删
	}

	@Override
	public void remove(String element) {
		String key = key();
		ActivityLocalRedis.getInstance().zrem(key, element);
		scoreMap.remove(element);
	}

	@Override
	public void doRank() {
		String key = key();
		if (key == null) {
			HawkLog.errPrintln("StrongestGuildTotalGuildRank key is null, curStage:{}", getStageId());
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrangeWithExipre(key, 0, Math.max((rankSize - 1), 0), StrongestGuildActivity.expireTime);
		for (Tuple tu : set) {
			String guildId = tu.getElement();
			if (!isExistGuild(guildId)) {
				HawkLog.logPrintln("StrongestGuildTotalGuildRank guild not exist, guildid: {}", guildId);
				continue;
			}
		}
		cacheRankList = exchangeSet(set);
	}
}
