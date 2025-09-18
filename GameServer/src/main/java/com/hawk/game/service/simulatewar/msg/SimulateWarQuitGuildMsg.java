package com.hawk.game.service.simulatewar.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.Player;

public class SimulateWarQuitGuildMsg extends HawkMsg {
	Player player;
	String guildId;	
	public SimulateWarQuitGuildMsg(Player player, String guildId) {
		this.player = player;
		this.guildId = guildId;
	}
	public Player getPlayer() {
		return player;
	}
	
	public String getGuildId() {
		return guildId;
	}
}
