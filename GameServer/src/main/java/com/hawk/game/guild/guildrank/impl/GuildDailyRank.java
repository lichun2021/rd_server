package com.hawk.game.guild.guildrank.impl;

import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;

import redis.clients.jedis.Tuple;

import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.LocalRedis;

//import org.hawk.os.HawkTime;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildBoardRank;

/**
 * 联盟每日排行日排行榜单
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午12:07:19
 */
public class GuildDailyRank extends GuildBoardRank {

	Logger logger = LoggerFactory.getLogger("Server");
	public GuildDailyRank(GRankType rType) {
		super(rType);
	}

	/**
	 * 内部函数
	 * 
	 * @Desc
	 * @param guildId
	 * @param pastDays
	 * @return
	 */
	final private String getRankKey(String guildId, int pastDays) {
		long curMs = HawkTime.getMillisecond();
		curMs -= HawkTime.DAY_MILLI_SECONDS * pastDays;
		String dateStr = HawkTime.formatTime(curMs, HawkTime.FORMAT_YMD);
		String key = String.format("%s:%s:%s", rankType.getTypeName(), guildId, dateStr);
		return key;
	}

	/**
	 * 获取排行列表
	 * 
	 * @Desc
	 * @param guildId
	 * @param pastDays
	 *            0表示今日榜单 1 表示昨日榜单 ...
	 * @return
	 */
	final public Set<Tuple> getRankList(String guildId, int pastDays) {
		try {
			String rankKey = getRankKey(guildId, pastDays);
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
	public Set<Tuple> getRankList(String guildId) {
		return getRankList(guildId, 0);
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		try {
			String key = getRankKey(guildId, 0);
			LocalRedis.getInstance().getRedisSession().zRem(key, 0, playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		try {
			String rankKey = getRankKey(guildId, 0);
			double afterAdd = LocalRedis.getInstance().getRedisSession().zIncrby(rankKey, playerId, val, rankType.GetOverdueTime());
			logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId, 1);
	}

}
