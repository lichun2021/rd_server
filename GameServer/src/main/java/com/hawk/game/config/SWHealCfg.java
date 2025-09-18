package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/sw_heal.xml")
public class SWHealCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final int deadNumMin;
	
	private final int deadNumMax;
	
	private final int healRate;
	
	public SWHealCfg() {
		id = 0;
		deadNumMin = 0;
		deadNumMax = 0;
		healRate = 0;
	}

	public int getId() {
		return id;
	}

	public int getDeadNumMin() {
		return deadNumMin;
	}

	public int getDeadNumMax() {
		return deadNumMax;
	}

	public int getHealRate() {
		return healRate;
	}

	
}

