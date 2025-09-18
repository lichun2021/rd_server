package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/cs/proxyNode.xml")
public class ProxyNodeCfg extends HawkConfigBase {
	@Id
	protected final String id;
	protected final String addr;
	protected final String areaId;
	
	public ProxyNodeCfg() {
		id = "";
		addr = "";
		areaId = "";
	}

	public String getId() {
		return id;
	}

	public String getAddr() {
		return addr;
	}

	public String getAreaId() {
		return areaId;
	}
}
