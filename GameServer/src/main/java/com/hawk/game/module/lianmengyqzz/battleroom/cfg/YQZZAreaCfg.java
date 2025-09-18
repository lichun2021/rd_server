package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/moon_war_area.xml")
public class YQZZAreaCfg extends HawkConfigBase {
	@Id
	private final int areaId;
	private final int buildId;
	private final int circle;

	public YQZZAreaCfg() {
		buildId = 0;
		areaId = 0;
		circle = 1;
	}

	public int getAreaId() {
		return areaId;
	}

	public int getBuildId() {
		return buildId;
	}

	public int getCircle() {
		return circle;
	}

}
