package com.hawk.game.service.simulatewar.msg;

import java.util.Map;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarBattleData;

public class SimulateWarSaveMarchMsg extends HawkMsg {
	Player player;
	Map<Integer, Integer> armyMap;
	PBSimulateWarBattleData.Builder battleData;
	public  SimulateWarSaveMarchMsg(Player player, PBSimulateWarBattleData.Builder data, Map<Integer, Integer> armyMap) {
		this.player = player;
		this.armyMap = armyMap;
		this.battleData = data;
	}
	
	public Player getPlayer() {
		return player;
	}	

	public PBSimulateWarBattleData.Builder getBattleData() {
		return battleData;
	}

	public Map<Integer, Integer> getArmyMap() {
		return armyMap;
	}	
}
