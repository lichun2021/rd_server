package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/battle_protect_level.xml")
public class BattleProtectLevelCfg extends HawkConfigBase {
	@Id
	protected final int levelgap;// ="1"
	protected final int triggerround;// ="5"
	protected final double lossrate;// ="0.5"
	protected final double percent;// ="0.2"

	public BattleProtectLevelCfg() {
		levelgap = 0;
		triggerround = 0;
		lossrate = 0;
		percent = 0;
	}

	public int getLevelgap() {
		return levelgap;
	}

	public int getTriggerround() {
		return triggerround;
	}

	public double getLossrate() {
		return lossrate;
	}

	public double getPercent() {
		return percent;
	}

}
