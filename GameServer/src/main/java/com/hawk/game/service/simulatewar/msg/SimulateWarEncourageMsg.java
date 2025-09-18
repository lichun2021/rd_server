package com.hawk.game.service.simulatewar.msg;

import org.hawk.msg.HawkMsg;

/**
 * 修改数据.
 * @author jm
 *
 */
public class SimulateWarEncourageMsg extends HawkMsg {
	String guildId;
	public SimulateWarEncourageMsg(String guildId) {
		this.guildId = guildId;
	}
	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
}
