package com.hawk.game.guild.guildrank;

import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;

/**
 * 个人榜单 基类
 * 
 * @author hawk
 *
 */
abstract public class GuildPersonalRank extends GuildBaseRank {
	protected GuildPersonalRank(GRankType rType) {
		super(rType);
	}
}
