package com.hawk.game.guild.guildrank.impl;

import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildBoardRank;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;

import redis.clients.jedis.Tuple;

/**
 * 联盟周榜实现类
 * 
 * @Desc 数据存储方式同联盟 数据过期时间走配置
 * @author RickMei
 * @Date 2018年11月15日 下午2:32:33
 */
public class GuildWeekRank extends GuildBoardRank {

	Logger logger = LoggerFactory.getLogger("Server");
	
	public GuildWeekRank(GRankType rType) {
		super(rType);
		// TODO Auto-generated constructor stub
	}

	public String getRankKey(String guildId, int pastDays) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(new Date());
		cal.add(Calendar.DATE, (0 - pastDays));
		int weekIndex = cal.get(Calendar.WEEK_OF_YEAR);
		String key = String.format("%s:%s:%d", rankType.getTypeName(), guildId, weekIndex);
		return key;
	}

	public Set<Tuple> getRankList(String guildId, int passDays) {
		try {
			String rankKey = getRankKey(guildId, passDays);
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
			String rankKey = getRankKey(guildId, 0);
			RedisProxy.getInstance().getRedisSession().zRem(rankKey, 0, playerId);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		// TODO Auto-generated method stub
		String rankKey = getRankKey(guildId, 0);
		try {
			double afterAdd = RedisProxy.getInstance().getRedisSession().zIncrby(rankKey, playerId, val, rankType.GetOverdueTime());
			logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId, 1);
	}

	@Override
	public Set<Tuple> getRankList(String guildId) {
		// TODO Auto-generated method stub
		return getRankList(guildId, 0);
	}
}
