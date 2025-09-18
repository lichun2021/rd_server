package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 进入联盟(创建,同意加入, 主动加入....)
 * 
 * @author Jesse
 *
 */
public class GuildJoinMsg extends HawkMsg {
	/**
	 * 联盟Id
	 */
	String guildId;

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public GuildJoinMsg() {
		super(MsgId.GUILD_QUIT_GUILD);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static GuildJoinMsg valueOf(String guildId) {
		GuildJoinMsg msg = new GuildJoinMsg();
		msg.guildId = guildId;
		return msg;
	}
}
