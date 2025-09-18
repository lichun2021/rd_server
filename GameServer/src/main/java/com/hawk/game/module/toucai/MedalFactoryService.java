package com.hawk.game.module.toucai;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.db.HawkDBManager;

public class MedalFactoryService {
	private static final MedalFactoryService instance = new MedalFactoryService();

	// 可偷取的玩家
	private Map<String, Long> lisenMap = new ConcurrentHashMap<>();
	
	public boolean init(){
		String sql = String.format("select playerId from medal_factory where canSteal = 1 limit 1000");

		List<String> pIds = HawkDBManager.getInstance().executeQuery(sql, null);
		pIds.forEach(id -> lisenMap.put(id, 0L));
		return true;
	}

	public static MedalFactoryService getInstance() {
		return instance;
	}

	public void putCanSteal(String playerId) {
		lisenMap.put(playerId, 0L);
	}

	public void removeCanSteal(String playerId) {
		lisenMap.remove(playerId);
	}

	public List<String> canStealList() {
		return new LinkedList<>(lisenMap.keySet());
	}
}
