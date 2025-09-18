package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "cfg/ipConfig.xml")
public class IpCfg extends HawkConfigBase {
	protected final String ip;
	protected final int value;

	public IpCfg() {
		this.ip = "";
		this.value = 0;
	}

	public String getIp() {
		return ip;
	}

	public int getValue() {
		return value;
	}
}
