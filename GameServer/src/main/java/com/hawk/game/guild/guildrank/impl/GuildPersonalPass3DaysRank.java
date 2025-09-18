package com.hawk.game.guild.guildrank.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

/**
 * 个人三日榜
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:33:47
 */
public class GuildPersonalPass3DaysRank extends GuildPersonalRank {
	Logger logger = LoggerFactory.getLogger("Server");
	public GuildPersonalPass3DaysRank(GRankType rankType) {
		super(rankType);
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		return true;
	}

	protected String getPlayerDayKey(String playerId, int pastDays) {
		long curMs = HawkTime.getMillisecond();
		curMs -= HawkTime.DAY_MILLI_SECONDS * pastDays;
		// fmt y-m-d
		String dateStr = HawkTime.formatTime(curMs, HawkTime.FORMAT_YMD);
		String key = String.format("%s:%s:%s", rankType.getTypeName(), playerId, dateStr);
		return key;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		try {
			if (!isClosed()) {
				String rankKey = getPlayerDayKey(playerId, 0);
				long afterAdd = LocalRedis.getInstance().getRedisSession().increaseBy(rankKey, val, rankType.GetOverdueTime()); // 数据保存4天
				logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	protected Set<Tuple> getRankListDayBefore(String guildId, int days) {
		TreeSet<Tuple> rankSet = new TreeSet<>();
		Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
		final int count = members.size();
		if (count > 0) {
			// 获取members的得分
			String[] memberIds = (String[]) members.toArray(new String[0]);
			List<Response<String>> piplineRes = new ArrayList<>();
			try (Jedis jedis = LocalRedis.getInstance().getRedisSession().getJedis();
					Pipeline pip = jedis.pipelined()) {
				for (String playerId : memberIds) {

					String key = getPlayerDayKey(playerId, days);
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

	public Set<Tuple> getRankList(String guildId, int pastDays) {

		TreeSet<Tuple> rankSet = new TreeSet<>();

		Set<Tuple> rank1 = getRankListDayBefore(guildId, pastDays);
		Set<Tuple> rank2 = getRankListDayBefore(guildId, pastDays + 1);
		Set<Tuple> rank3 = getRankListDayBefore(guildId, pastDays + 2);

		HashMap<String, Tuple> rankMap = new HashMap<String, Tuple>();
		for (Tuple iter : rank1) {
			rankMap.put(iter.getElement(), iter);
		}

		for (Tuple iter : rank2) {
			Tuple mapIter = rankMap.get(iter.getElement());
			if (null != mapIter) {
				double newScore = mapIter.getScore() + iter.getScore();
				String key = iter.getElement();
				rankMap.remove(iter.getElement());
				rankMap.put(key, new Tuple(key, newScore));
			} else {
				rankMap.put(iter.getElement(), iter);
			}
		}

		for (Tuple iter : rank3) {
			Tuple mapIter = rankMap.get(iter.getElement());
			if (null != mapIter) {
				double newScore = mapIter.getScore() + iter.getScore();
				String key = iter.getElement();
				rankMap.remove(iter.getElement());
				rankMap.put(key, new Tuple(key, newScore));
			} else {
				rankMap.put(iter.getElement(), iter);
			}
		}
		// 取三天平均值
		for (Map.Entry<String, Tuple> entry : rankMap.entrySet()) {
			// 避免出现0的情况
			if (entry.getValue().getScore() / 3 > 1) {
				rankSet.add(new GuildRankTuple(entry.getKey(), entry.getValue().getScore() / 3));
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
