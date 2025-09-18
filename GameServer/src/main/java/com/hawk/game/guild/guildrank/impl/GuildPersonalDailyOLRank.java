package com.hawk.game.guild.guildrank.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

/**
 * 联盟榜单在线时间榜单
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:33:18
 */
public class GuildPersonalDailyOLRank extends GuildPersonalDailyRank {

	public GuildPersonalDailyOLRank(GRankType rType) {
		super(rType);
	}

	/**
	 * 往昨日榜单添加数据
	 * 
	 * @Desc 为了在跨天时做在线时长统计
	 * @param playerId
	 * @param val
	 */
	void addValToYesterdayRank(String playerId, long val) {
		try {
			if (!isClosed()) {
				String key = getPlayerDayKey(playerId, 1);
				long afterAdd = LocalRedis.getInstance().getRedisSession().increaseBy(key, val, rankType.GetOverdueTime()); // 数据保存两天
				logger.info("guildrank_log {} player:{} add yesterday:{} after:{}",rankType.getTypeName(), playerId, val, afterAdd);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void onPassDay() {
		// 当前在线的玩家的在线时间记录到redis
		long curTimeMs = HawkTime.getMillisecond();
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			Collection<String> playerIds = GuildService.getInstance().getGuildMembers(guildId);
			for (String playerId : playerIds) {
				// 如果在线并且 登录时间跟当前时间不是同一天
				if (GlobalData.getInstance().isOnline(playerId)) {
					long loginTimeMs = GlobalData.getInstance().getActivePlayer(playerId).getLoginTime();
					if (!HawkTime.isSameDay(loginTimeMs, curTimeMs) && curTimeMs > loginTimeMs) {
						// 跨天处理 昨天登录时间加上昨天零点的时间加到昨日在线时长
						if (HawkTime.getAM0Date().getTime() < loginTimeMs + HawkTime.DAY_MILLI_SECONDS) {
							addValToYesterdayRank(playerId, (HawkTime.getAM0Date().getTime() - loginTimeMs) / 1000);
						} else {
							addValToYesterdayRank(playerId, HawkTime.DAY_MILLI_SECONDS / 1000 - 1);
						}
					}
				}
			}
		}
		super.onPassDay();
	}

	public Set<Tuple> getRankList(String guildId, int pastDays) {
		if (pastDays == 0) {
			Set<Tuple> retSet = super.getRankList(guildId, pastDays);
			// 在线玩家要 加上 登录时间和当前时间
			TreeSet<Tuple> newRet = new TreeSet<>();
			Collection<String> guildPlayerIds = GuildService.getInstance().getGuildMembers(guildId);
			long curTimeMs = HawkTime.getMillisecond();
			for (String playerId : guildPlayerIds) {
				double onlineTime = 0;
				for (Tuple tp : retSet) {
					if (tp.getElement().equals(playerId)) {
						onlineTime = tp.getScore();
					}
				}

				if (GlobalData.getInstance().isOnline(playerId)) {
					long loginTimeMs = GlobalData.getInstance().getActivePlayer(playerId).getLoginTime();
					if (loginTimeMs < curTimeMs) {
						if (HawkTime.isSameDay(loginTimeMs, curTimeMs)) {
							onlineTime += (curTimeMs - loginTimeMs) / 1000; // 上下线时间存的是秒这个没有办法
						} else {
							long dayBeginMs = HawkTime.getAM0Date().getTime();
							onlineTime += (curTimeMs - dayBeginMs) / 1000;
						}
					}
				}

				if (onlineTime > 0)
					newRet.add(new GuildRankTuple(playerId, onlineTime));
			}
			if (rankType.isOrderSmallToLarge())
				return newRet;
			else
				return newRet.descendingSet();
		} else {
			Set<Tuple> retSet = super.getRankList(guildId, pastDays);
			return retSet;
		}
	}

	@Override
	public Set<Tuple> getRankList(String guildId) {
		return getRankList(guildId, 0);
	}

}
