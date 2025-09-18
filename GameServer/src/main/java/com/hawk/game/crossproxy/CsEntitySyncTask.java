package com.hawk.game.crossproxy;

import org.hawk.thread.HawkTask;

import com.hawk.game.player.cache.PlayerDataSerializer;

public class CsEntitySyncTask extends HawkTask {
	private String playerId;
	
	public CsEntitySyncTask(String playerId) {
		setTypeName("CsEntitySync");
		setMustRun(true);
		this.playerId = playerId;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	@Override
	public Object run() {
		PlayerDataSerializer.csSyncPlayerData(playerId);
		return null;
	}
}
