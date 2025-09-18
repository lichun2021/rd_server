package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_color_quality.xml")
public class SuperSoldierColorQualityCfg extends HawkConfigBase {
	@Id
	protected final int quality;// ="1"
	protected final int maxStarLevel;// ="60"

	public SuperSoldierColorQualityCfg() {
		this.quality = 1;
		this.maxStarLevel = 10;
	}

	public int getQuality() {
		return quality;
	}

	public int getMaxStarLevel() {
		return maxStarLevel;
	}

}
