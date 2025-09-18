package com.hawk.activity.type.impl.domeExchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/empty_exchange/empty_exchange_cfg.xml")
public class DomeActivityKVConfig extends HawkConfigBase {
	
	private final int serverDelay;
	
	public DomeActivityKVConfig(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
}
