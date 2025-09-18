package com.hawk.game.service.simulatewar.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.Player;

public class SimulateWarDissolveMsg extends HawkMsg {
	Player player;
	String marchId;
	public SimulateWarDissolveMsg(Player player, String marchId) {
		this.player = player;
		this.marchId = marchId;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public String getMarchId() {
		return marchId;
	}
	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}
}
