package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 退出联盟
 * 
 * @author Jesse
 *
 */
public class TiberiumWarEnterMsg extends HawkMsg {
	/**
	 * 是否被踢出
	 */
	boolean isKick;
	/**
	 * 联盟Id
	 */
	String guildId;
	
	public boolean isKick() {
		return isKick;
	}

	public void setKick(boolean isKick) {
		this.isKick = isKick;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	public TiberiumWarEnterMsg() {
		super(MsgId.GUILD_QUIT_GUILD);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static TiberiumWarEnterMsg valueOf(boolean isKick, String guildId) {
		TiberiumWarEnterMsg msg = new TiberiumWarEnterMsg();
		msg.guildId = guildId;
		msg.isKick = isKick;
		return msg;
	}
}
