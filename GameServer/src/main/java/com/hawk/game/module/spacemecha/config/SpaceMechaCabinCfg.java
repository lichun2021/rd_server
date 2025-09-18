package com.hawk.game.module.spacemecha.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_cabin.xml")
public class SpaceMechaCabinCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int level;
	
	protected final int gridCnt;
	
	protected final int cost;
	
	protected final int blood;
	
	protected final String winAward;
	
	protected final String winPartiAward;
	
	protected final String loseAward;
	
	public static Map<Integer, SpaceMechaCabinCfg> levelCfgMap = new HashMap<>();

	public SpaceMechaCabinCfg() {
		id = 0;
		level = 0;
		gridCnt = 0;
		cost = 0;
		blood = 0;
		winAward = "";
		winPartiAward  = "";
		loseAward = "";
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

	public int getCost() {
		return cost;
	}

	public int getBlood() {
		return blood;
	}

	public String getWinAward() {
		return winAward;
	}

	public String getWinPartiAward() {
		return winPartiAward;
	}

	public String getLoseAward() {
		return loseAward;
	}
	
	public boolean assemble() {
		levelCfgMap.put(level, this);
		return true;
	}

	public static SpaceMechaCabinCfg getCfgByLevel(int level) {
		return levelCfgMap.get(level);
	}
}
