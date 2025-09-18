package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月12日
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_warehouse.xml")
public class GuildManorWareHouseCfg extends HawkConfigBase {
	/** Id */
	@Id
	protected final int id;

	protected final String defaultName;

	protected final int buildingUpLimit;
	protected final int saveUpLimit;// ="3000000"
	protected final int everydayUpLimit;// ="188000"

	public GuildManorWareHouseCfg() {
		id = 0;
		defaultName = "";
		buildingUpLimit = 0;
		saveUpLimit = 0;
		everydayUpLimit = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public int getBuildingUpLimit() {
		return buildingUpLimit;
	}

	public int getSaveUpLimit() {
		return saveUpLimit;
	}

	public int getEverydayUpLimit() {
		return everydayUpLimit;
	}

}
