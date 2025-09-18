package com.hawk.game.guild.guildrank.impl;

import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildBoardRank;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;

import redis.clients.jedis.Tuple;

/**
 * 联盟总榜
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:34:43
 */
public class GuildTotalRank extends GuildBoardRank {
	
	Logger logger = LoggerFactory.getLogger("Server");
	
	public GuildTotalRank(GRankType rType) {
		super(rType);
	}

	public String getRankKey(String guildId) {
		String rankKey = String.format("%s:%s", rankType.getTypeName(), guildId);
		return rankKey;
	}

	@Override
	public Set<Tuple> getRankList(String guildId) {
		try {
			String rankKey = getRankKey(guildId);
			Set<Tuple> tmpSet = RedisProxy.getInstance().getRedisSession().zRangeWithScores(rankKey, 0, -1, 0);
			if (rankType.isOrderSmallToLarge()) {
				return tmpSet;
			} else {
				return GuildRankTuple.descendingRankSet(tmpSet);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return new TreeSet<Tuple>();
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		try {
			String rankKey = getRankKey(guildId);
			RedisProxy.getInstance().getRedisSession().zRem(rankKey, 0, playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		try {
			String rankKey = getRankKey(guildId);
			double afterAdd = RedisProxy.getInstance().getRedisSession().zIncrby(rankKey, playerId, val, rankType.GetOverdueTime());
			logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId);
	}
}