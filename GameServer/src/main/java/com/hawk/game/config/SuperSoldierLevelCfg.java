package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_level.xml")
@HawkConfigBase.CombineId(fields = { "heroLevel", "heroQuality" })
public class SuperSoldierLevelCfg extends HawkConfigBase {
	protected final int supersoldierLevel;// ="14"
	protected final int supersoldierQuality;
	protected final int levelUpExp;// ="44240" 满经验. 即升级经验.

	public SuperSoldierLevelCfg() {
		this.supersoldierLevel = 0;
		this.levelUpExp = 0;
		supersoldierQuality = 0;
	}

	public int getSupersoldierLevel() {
		return supersoldierLevel;
	}

	public int getSupersoldierQuality() {
		return supersoldierQuality;
	}

	public int getLevelUpExp() {
		return levelUpExp;
	}


}
