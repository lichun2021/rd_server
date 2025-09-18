package com.hawk.game.guild.guildrank.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

/**
 * 联盟榜单 入盟时间榜单
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月15日 下午3:31:54
 */
public class GuildJoinGuildRank extends GuildTotalRank {

	public GuildJoinGuildRank(GRankType rType) {
		super(rType);
	}

	@Override
	public void addRankVal(String guildid, String playerId, long val) {
		try {
			if (!isClosed()) {
				String rankKey = getRankKey(guildid);
				RedisProxy.getInstance().getRedisSession().zAdd(rankKey, val, playerId, rankType.GetOverdueTime());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public Set<Tuple> getRankList(String guildId) {
		// 获取redis中的排行数据
		Set<Tuple> zrangeSet = super.getRankList(guildId);
		// 转换成 Set<GuildRankTuple>
		Set<Tuple> realSortedSet = new TreeSet<>();
		List<String> zsetGetMemberId = new ArrayList<String>();
		zrangeSet.forEach(e -> {
			zsetGetMemberId.add(e.getElement());
			realSortedSet.add(new GuildRankTuple(e.getElement(), e.getScore()));
		});
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		// 去重筛选出来联盟的创建人
		memberIds.removeAll(zsetGetMemberId);
		if (!memberIds.isEmpty()) {
			for (String tmpGuildId : memberIds) {
				realSortedSet.add(new GuildRankTuple(tmpGuildId,
						(double) (GuildService.getInstance().getGuildCreateTime(guildId) / 1000)));
			}
		}
		if (rankType.isOrderSmallToLarge()) {
			return realSortedSet;
		} else {
			return GuildRankTuple.descendingRankSet(realSortedSet);
		}
	}
}
