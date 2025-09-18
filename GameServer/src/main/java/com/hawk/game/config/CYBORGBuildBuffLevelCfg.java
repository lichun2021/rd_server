package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_build_buff_level.xml")
public class CYBORGBuildBuffLevelCfg extends HawkConfigBase {
	// <data id="10001" level="1" buffId="1" buffList="100_5000,136_5000,102_5000" />
	@Id
	protected final int id;
	protected final int exp;
	protected final int level;

	public CYBORGBuildBuffLevelCfg() {
		this.id = 0;
		this.exp = 0;
		this.level = 0;
	}

	public int getId() {
		return id;
	}

	public int getExp() {
		return exp;
	}

	public int getLevel() {
		return level;
	}

}
