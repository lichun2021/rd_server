package com.hawk.game.lianmengjunyan.player.npc.cache;

import com.google.common.cache.CacheLoader;
import com.hawk.game.config.LMJYNpcCfg;

public class LMJYNPCPlayerDataLoader extends CacheLoader<LMJYNPCPlayerDataKey, Object> {
	/**
	 * 玩家id
	 */
	private String playerId;
	private LMJYNpcCfg npcCfg;

	public LMJYNPCPlayerDataLoader(String playerId, LMJYNpcCfg npcCfg) {
		this.playerId = playerId;
		this.npcCfg = npcCfg;
	}

	@Override
	public Object load(LMJYNPCPlayerDataKey key) throws Exception {
		return key.load(playerId, npcCfg);
	}
}
