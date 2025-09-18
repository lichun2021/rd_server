package com.hawk.game.module.SampleFight.hero;

import java.util.HashMap;
import java.util.Map;

import com.hawk.game.player.hero.PlayerHero;

public class SFHeroFactory {
	private static Map<Integer, SFHero> Map = new HashMap<>();
	public static PlayerHero getHerobyCfgId(int cfgId){
		if(!Map.containsKey(cfgId)){
			Map.put(cfgId, SFHero.create(cfgId));
		}
		return Map.get(cfgId);
	}
}
