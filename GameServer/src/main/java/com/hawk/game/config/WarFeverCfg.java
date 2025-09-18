package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战争狂热
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_warFever.xml")
public class WarFeverCfg extends HawkConfigBase {
	/**
	 * 配置id
	 */
	@Id
	private final int cityLevel;
	
	private final int warFeverTime;
	
	public WarFeverCfg() {
		cityLevel = 0;
		warFeverTime = 0;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public long getWarFeverTime() {
		return warFeverTime * 1000L;
	}
}
