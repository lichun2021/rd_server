package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月12日
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_tower.xml")
public class GuildManorTowerCfg extends HawkConfigBase {
	
	/** Id */
	@Id
	protected final int id; 
	
	protected final String defaultName; 
	
	protected final int towerRadius; 
	
	protected final int towerAttack;
	/** 攻击频率   Ns/次*/
	protected final int atkPeriod;
	
	protected final int buildingUpLimit;
	
	protected final int manorCount;
	
	public GuildManorTowerCfg() {
		id = 0;
		defaultName = "";
		towerRadius = 0;
		towerAttack = 0;
		atkPeriod = 0;
		buildingUpLimit = 0;
		manorCount = 0;
	}

	public int getId() {
		return id;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public int getTowerRadius() {
		return towerRadius;
	}

	public int getTowerAttack() {
		return towerAttack;
	}
	
	public int getAtkPeriod() {
		return atkPeriod;
	}

	public int getBuildingUpLimit() {
		return buildingUpLimit;
	}

	public int getManorCount() {
		return manorCount;
	}
}
