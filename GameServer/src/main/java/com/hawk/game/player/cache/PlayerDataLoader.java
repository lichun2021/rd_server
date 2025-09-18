package com.hawk.game.player.cache;

import com.google.common.cache.CacheLoader;
import com.hawk.game.global.GlobalData;

public class PlayerDataLoader extends CacheLoader<PlayerDataKey, Object> {
	/**
	 * 玩家id
	 */
	private String playerId;

	public PlayerDataLoader(String playerId) {
		this.playerId = playerId;
	}

	@Override
	public Object load(PlayerDataKey key) throws Exception {
		// 是否为新玩家的判断
		boolean newly = GlobalData.getInstance().isNewlyPlayer(playerId);
		return key.load(playerId, newly);
	}
}
