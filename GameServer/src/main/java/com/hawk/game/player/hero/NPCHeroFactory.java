package com.hawk.game.player.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.player.supersoldier.NPCSuperSoldier;
import com.hawk.game.util.GameUtil;

public class NPCHeroFactory {
	private static NPCHeroFactory Instance = new NPCHeroFactory();
	private static Map<FoggyHeroCfg, NPCHero> Map = new HashMap<>();
	private static Map<Integer, NPCSuperSoldier> SuperSoldierMap = new HashMap<>();

	public static NPCHeroFactory getInstance() {
		return Instance;
	}

	/**
	 * 
	 * @param cfgId
	 *            foggy_hero.xml
	 * @return
	 */
	public NPCHero get(int cfgId) {
		FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, cfgId);
		if (Objects.isNull(cfg)) {
			return null;
		}
		if (Map.containsKey(cfg)) {
			return Map.get(cfg);
		}

		NPCHero result = NPCHero.create(cfg);
		Map.put(cfg, result);
		return result;
	}

	public List<PlayerHero> get(List<Integer> cfgIds) {
		HawkAssert.notNull(cfgIds, "list must not be null!");
		List<PlayerHero> heroList = new ArrayList<>();
		for (int cfgId : cfgIds) {

			FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, cfgId);
			if (Objects.isNull(cfg)) {
				continue;
			}
			NPCHero result;
			if (Map.containsKey(cfg)) {
				result = Map.get(cfg);
			} else {
				result = NPCHero.create(cfg);
				Map.put(cfg, result);
			}
			heroList.add(result);
		}
		return heroList;
	}

	public NPCSuperSoldier getSuperSoldier(int id, int star) {
		int key = GameUtil.combineXAndY(id, star);
		if (SuperSoldierMap.containsKey(key)) {
			return SuperSoldierMap.get(key);
		}

		NPCSuperSoldier result = NPCSuperSoldier.create(id, star);
		SuperSoldierMap.put(key, result);
		return result;
	}
}
