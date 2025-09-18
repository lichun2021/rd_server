package com.hawk.game.guild.guildrank.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

/**
 * 三日在线
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:32:59
 */
public class GuildPersonal3DaysOLRank extends GuildPersonalPass3DaysRank {
	Logger logger = LoggerFactory.getLogger("Server");
			
	public GuildPersonal3DaysOLRank(GRankType rankType) {
		super(rankType);
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		return true;
	}

	/**
	 * 往昨日榜单添加数据
	 * 
	 * @Desc 为了在跨天时做在线时长统计
	 * @param playerId
	 * @param val
	 */
	void addValToYesterdayRank(String playerId, int val) {
		try {
			if (!isClosed()) {
				String key = getPlayerDayKey(playerId, 1);
				long afterAdd = LocalRedis.getInstance().getRedisSession().increaseBy(key, val, rankType.GetOverdueTime());
				logger.info("guildrank_log {} player:{} add yesterday:{} after:{}", rankType.getTypeName(), playerId, val, afterAdd);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void onPassDay() {
		// 当前在线的玩家的在线时间记录到redis
		int oneDaySecs = 86400;
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
						int onlineTime = (int) ((HawkTime.getAM0Date().getTime() - loginTimeMs) / 1000);
						
						if(onlineTime >= oneDaySecs){
							onlineTime = oneDaySecs - 1;
						}
						
						addValToYesterdayRank(playerId, onlineTime);
					}
				}
			}
		}
		super.onPassDay();
	}

	@Override
	protected Set<Tuple> getRankListDayBefore(String guildId, int days) {

		Set<Tuple> retSet = super.getRankListDayBefore(guildId, days);
		if (days == 0) { // 当天的需要取在线的数据
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
							onlineTime = (curTimeMs - dayBeginMs) / 1000;
						}
					}
				}

				if (onlineTime > 0)
					newRet.add(new Tuple(playerId, onlineTime));
			}
			if (rankType.isOrderSmallToLarge())
				return newRet;
			else
				return newRet.descendingSet();
		} else
			return retSet;

	}

}
