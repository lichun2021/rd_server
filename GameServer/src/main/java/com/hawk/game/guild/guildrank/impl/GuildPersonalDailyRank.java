package com.hawk.game.guild.guildrank.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildPersonalRank;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * 个人今日榜单
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:33:32
 */
public class GuildPersonalDailyRank extends GuildPersonalRank {
	
	Logger logger = LoggerFactory.getLogger("Server");
	
	public GuildPersonalDailyRank(GRankType rankType) {
		super(rankType);
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		return true;
	}

	/*
	 * 获取玩家key passDay 过去的天数
	 */
	protected String getPlayerDayKey(String playerId, int passDays) {

		long curMs = HawkTime.getMillisecond();
		curMs -= HawkTime.DAY_MILLI_SECONDS * passDays;

		// fmt y-m-d
		String dateStr = HawkTime.formatTime(curMs, HawkTime.FORMAT_YMD);
		String key = String.format("%s:%s:%s", rankType.getTypeName(), playerId, dateStr);
		return key;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		try {
			if (!isClosed()) {
				String key = getPlayerDayKey(playerId, 0);
				long afterAdd = LocalRedis.getInstance().getRedisSession().increaseBy(key, val, rankType.GetOverdueTime()); // 数据保存两天
				logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	public Set<Tuple> getRankList(String guildId, int pastDays) {
		// 个人每日榜单存本地redis
		// 获取联盟内玩家id
		Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
		final int count = members.size();
		TreeSet<Tuple> rankSet = new TreeSet<>();
		if (count > 0) {
			// 获取members的得分
			String[] memberIds = (String[]) members.toArray(new String[0]);
			List<Response<String>> piplineRes = new ArrayList<>();
			try (Jedis jedis = LocalRedis.getInstance().getRedisSession().getJedis();
					Pipeline pip = jedis.pipelined()) {
				for (String playerId : memberIds) {
					String key = getPlayerDayKey(playerId, pastDays);
					Response<String> onePiplineRes = pip.get(key);
					piplineRes.add(onePiplineRes);
				}

				pip.sync();

				if (piplineRes.size() == count) {
					for (int i = 0; i < count; i++) {
						String retStr = piplineRes.get(i).get();
						if (null != retStr) {
							rankSet.add(new GuildRankTuple(memberIds[i], (double) Long.parseLong(retStr)));
						}
					}
				}

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		if (rankType.isOrderSmallToLarge())
			return rankSet;
		else
			return rankSet.descendingSet();
	}

	@Override
	public Set<Tuple> getRankList(String guildId) {
		return getRankList(guildId, 0);
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId, 1);
	}
}