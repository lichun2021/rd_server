package com.hawk.game.msg;

import java.util.Set;

import org.hawk.msg.HawkMsg;

public class UpdateGuardDressMsg extends HawkMsg {
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 当前拥有的装扮ID
	 */
	private Set<Integer> hasDressIdSet;
	
	public UpdateGuardDressMsg(String playerId, Set<Integer> hasDressIdSet) {
		this.playerId = playerId;
		this.hasDressIdSet = hasDressIdSet;
	}

	public String getPlayerId() {
		return playerId;
	}

	public Set<Integer> getHasDressIdSet() {
		return hasDressIdSet;
	}
}
