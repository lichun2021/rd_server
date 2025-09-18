package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 体力消耗
 * 
 */
public class PlayerVitCostMsg extends HawkMsg {
	/**
	 * 体力消耗
	 */
	private int cost;
	private String playerId;

	public int getCost() {
		return cost;
	}

	public String getPlayerId() {
		return playerId;
	}

	public static PlayerVitCostMsg valueOf(String playerId, int vitCost) {
		PlayerVitCostMsg result = new PlayerVitCostMsg();
		result.cost = vitCost;
		result.playerId = playerId;
		return result;
	}

}
