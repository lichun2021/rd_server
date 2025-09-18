package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "cfg/serverFeature.xml")
public class ServerFeatureCfg extends HawkConfigBase {
	@Id
	protected final String serverId;
	
	public ServerFeatureCfg() {
		this.serverId = "";
	}

	public String getServerId() {
		return serverId;
	}
}
