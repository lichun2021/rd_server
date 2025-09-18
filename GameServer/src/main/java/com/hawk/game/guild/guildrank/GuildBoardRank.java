package com.hawk.game.guild.guildrank;

import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;

/**
 * 联盟榜单基类
 * 
 * @author hawk
 *
 */
abstract public class GuildBoardRank extends GuildBaseRank {
	protected GuildBoardRank(GRankType rType) {
		super(rType);
		isGuildRank = true;
	}
}
