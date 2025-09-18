package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 消息 - 通过联盟申请消息
 * 
 * @author Jesse
 *
 */
public class AcceptGuildApplyMsg extends HawkMsg {
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

	public AcceptGuildApplyMsg() {
		super(MsgId.ACCEPT_APPLY_GUILD);
	}
	
	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static AcceptGuildApplyMsg valueOf(String guildId) {
		AcceptGuildApplyMsg msg = new AcceptGuildApplyMsg();
		msg.guildId = guildId;
		return msg;
	}
}
