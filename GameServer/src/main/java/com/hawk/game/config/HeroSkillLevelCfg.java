package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/hero_skill_level.xml")
@HawkConfigBase.CombineId(fields = { "skillLevel", "skillQuality" })
public class HeroSkillLevelCfg extends HawkConfigBase {

	protected final int id;// ="1"
	protected final int skillLevel;// ="1"
	protected final int skillQuality;
	protected final int skillExp;// ="200"

	public HeroSkillLevelCfg() {
		skillLevel = 1;
		skillExp = 0;
		id = 0;
		skillQuality = 1;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public int getSkillExp() {
		return skillExp;
	}

	public int getId() {
		return id;
	}

	public int getSkillQuality() {
		return skillQuality;
	}

}
