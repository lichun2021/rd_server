package com.hawk.game.module.spacemecha.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 *
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_subcabin.xml")
public class SpaceMechaSubcabinCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int level;
	
	protected final int gridCnt;
	
	protected final int blood;
	/**
	 * 子舱可以进驻的人员上限
	 */
	protected final int guardLimit;
	
	protected final String winAward;
	/**
	 * 防守成功后加至主舱的buff
	 */
	protected final String winEffect;
	
	private Map<Integer, Integer> winEffectMap;
	private static Map<Integer, SpaceMechaSubcabinCfg> cfgMap = new HashMap<>();

	public SpaceMechaSubcabinCfg() {
		id = 0;
		level = 0;
		gridCnt = 0;
		blood = 0;
		guardLimit = 0;
		winAward = "";
		winEffect = "";
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getGridCnt() {
		return gridCnt;
	}

	public int getBlood() {
		return blood;
	}

	public String getWinAward() {
		return winAward;
	}

	public int getGuardLimit() {
		return guardLimit;
	}
	
	public boolean assemble() {
		winEffectMap = SerializeHelper.stringToMap(winEffect, Integer.class, Integer.class, "_", ",");
		cfgMap.put(level, this);
		return true;
	}
	
	public Map<Integer, Integer> getWinEffectMap() {
		return winEffectMap;
	} 
	
	public static SpaceMechaSubcabinCfg getCfg(int level) {
		return cfgMap.get(level);
	}
	
}
