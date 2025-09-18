package com.hawk.game.strengthenguide;

import java.util.HashMap;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.strengthenguide.entity.SGPlayerEntity;

public class StrengthenGuideData {
	private static HashMap<String, SGPlayerEntity> entities = new HashMap<>();

	public static void onPlayerLogin(String playerId) {

		if (null == entities.get(playerId)) {
			// 取数据库加载
			SGPlayerEntity entity = LocalRedis.getInstance().loadPlayerStrengthenGuideScore(playerId);

			if (null != entity && entity.getPlayerId().equals(playerId)) {
				entities.put(playerId, entity);
			}
		}
	}

	static public void put(String key, SGPlayerEntity value) {
		entities.put(key, value);
	}

	static public SGPlayerEntity get(String key) {
		return entities.get(key);
	}
}
