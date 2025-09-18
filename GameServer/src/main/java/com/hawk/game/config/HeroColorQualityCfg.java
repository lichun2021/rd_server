package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/hero_color_quality.xml")
public class HeroColorQualityCfg extends HawkConfigBase {
	@Id
	protected final int quality;// ="1"
	protected final int maxStarLevel;// ="60"

	public HeroColorQualityCfg() {
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
