package com.hawk.game.service.simulatewar.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.SimulateWar.WayType;

public class SimulateWarOrderAdjustMsg extends HawkMsg {
	Player player;
	String guildId;
	WayType wayType;
	List<String> marchIdList;
	public SimulateWarOrderAdjustMsg(Player player, String guildId, WayType wayType, List<String> marchIdList) {
		this.player = player;
		this.guildId = guildId;
		this.wayType = wayType;
		this.marchIdList = marchIdList;
	}
	public Player getPlayer() {
		return player;
	}
	public String getGuildId() {
		return guildId;
	}
	public WayType getWayType() {
		return wayType;
	}
	public List<String> getMarchIdList() {
		return marchIdList;
	}
}
