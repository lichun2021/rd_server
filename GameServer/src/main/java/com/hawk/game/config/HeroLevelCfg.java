package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/hero_level.xml")
@HawkConfigBase.CombineId(fields = { "heroLevel", "heroQuality" })
public class HeroLevelCfg extends HawkConfigBase {
	protected final int heroLevel;// ="14"
	protected final int heroQuality;
	protected final int levelUpExp;// ="44240" 满经验. 即升级经验.

	public HeroLevelCfg() {
		this.heroLevel = 0;
		this.levelUpExp = 0;
		heroQuality = 0;
	}

	public int getHeroLevel() {
		return heroLevel;
	}

	public int getLevelUpExp() {
		return levelUpExp;
	}

	public int getHeroQuality() {
		return heroQuality;
	}

}
