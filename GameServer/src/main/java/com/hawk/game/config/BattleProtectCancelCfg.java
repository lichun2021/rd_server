package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/battle_protect_cancel.xml")
public class BattleProtectCancelCfg extends HawkConfigBase {
	@Id
	protected final int level;// ="1"
	protected final int num;// ="2000"

	public BattleProtectCancelCfg() {
		level = 0;
		num = 0;
	}

	public int getLevel() {
		return level;
	}

	public int getNum() {
		return num;
	}

}
