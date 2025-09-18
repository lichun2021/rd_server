package com.hawk.game.service.simulatewar.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.SimulateWar.WayType;

public class SimulateWarAdjustWayMsg extends HawkMsg {
	Player player;
	String marchId;
	WayType wayType;
	public SimulateWarAdjustWayMsg(Player player, WayType wayType, String marchId) {
		this.player = player;
		this.wayType = wayType;
		this.marchId = marchId;
	}
	public Player getPlayer() {
		return player;
	}
	
	public String getMarchId() {
		return marchId;
	}
	
	public WayType getWayType() {
		return wayType;
	}
}
