package com.hawk.activity.type.impl.greetings.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/blessing/blessing_activity_cfg.xml")
public class GreetingsActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	
	public GreetingsActivityKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
