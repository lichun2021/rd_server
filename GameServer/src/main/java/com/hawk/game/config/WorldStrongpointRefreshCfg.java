package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 世界据点刷新配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_strongpoint_refresh.xml")
public class WorldStrongpointRefreshCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int openServiceTimeLowerLimit;
	
	protected final int openServiceTimeUpLimit;
	
	protected final int commonNum;
	
	protected final int specialNum;
	
	protected final int capitalNum;
	
	public WorldStrongpointRefreshCfg() {
		id = 0;
		openServiceTimeLowerLimit = 0;
		openServiceTimeUpLimit = 0;
		commonNum = 0;
		specialNum = 0;
		capitalNum = 0;
	}

	public int getId() {
		return id;
	}

	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public int getOpenServiceTimeUpLimit() {
		return openServiceTimeUpLimit;
	}

	public int getCommonNum() {
		return commonNum;
	}

	public int getSpecialNum() {
		return specialNum;
	}

	public int getCapitalNum() {
		return capitalNum;
	}

	@Override
	protected boolean assemble() {
		return true;
	}
}
