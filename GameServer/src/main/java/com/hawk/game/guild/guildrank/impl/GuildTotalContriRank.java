package com.hawk.game.guild.guildrank.impl;

import org.hawk.os.HawkException;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;

/**
 * 联盟总榜
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:34:43
 */
public class GuildTotalContriRank extends GuildTotalRank {
	public GuildTotalContriRank(GRankType rType) {
		super(rType);
	}

	/**
	 * 设置捐献值
	 * 
	 * @Desc 用来作为初次加载数据时的捐献值设置
	 * @param guildId
	 * @param playerId
	 * @param val
	 */
	public void setRankVal(String guildId, String playerId, long val) {
		try {
			String rankKey = getRankKey(guildId);
			double afterAdd = RedisProxy.getInstance().getRedisSession().zAdd(rankKey, val, playerId, rankType.GetOverdueTime());
			logger.info("guildrank_log {}, guildId:{} player:{} init add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

}