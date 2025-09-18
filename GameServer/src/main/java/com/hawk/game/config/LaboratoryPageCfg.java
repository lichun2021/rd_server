package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/laborary_page.xml")
public class LaboratoryPageCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final String unlockCost;

	public LaboratoryPageCfg() {
		id = 0;
		unlockCost = "";
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public String getUnlockCost() {
		return unlockCost;
	}

}
