package com.hawk.activity.type.impl.strongestGuild.rank.impl;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

/***
 * 王者联盟 联盟阶段排行榜
 * 
 * @author yang.rao
 *
 */

public class StrongestGuildStageGuildRank implements StrongestGuildRank {

	private Set<Tuple> cacheRankList = new LinkedHashSet<>();

	@Override
	public String key() {
		int termId = getTermId();
		int stageId = getStageId();
		if (termId == 0 || stageId == 0) {
			return null;
		}
		String key = String.format(ActivityRedisKey.STRONGEST_GUILD_STAGE_RANK, termId, stageId);
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
		ActivityLocalRedis.getInstance().zIncrby(key(), guildId, score);
		return true;
	}

	@Override
	public double getScore(String guildId) {
		Double score = ActivityLocalRedis.getInstance().zScore(key(), guildId);
		double s = score == null ? 0 : score;
		return s;
	}

	@Override
	public void clear() {
	}

	@Override
	public void remove(String element) {
		String key = key();
		ActivityLocalRedis.getInstance().zrem(key, element);
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
	public void doRank() {
		String key = key();
		if (key == null) {
			HawkLog.errPrintln("StrongestGuildStageGuildRank key is null, curStage:{}", getStageId());
			return;
		}
		int rankSize = getRankSize();
		Set<Tuple> set = ActivityLocalRedis.getInstance().zrevrangeWithExipre(key, 0, Math.max((rankSize - 1), 0), StrongestGuildActivity.expireTime);
		for (Tuple tu : set) {
			String guildId = tu.getElement();
			if (!isExistGuild(guildId)) {
				HawkLog.logPrintln("StrongestGuildRank guild not exist, guildid: {}", guildId);
			}
		}
		// 转换一下积分
		cacheRankList = exchangeSet(set);
	}
}
