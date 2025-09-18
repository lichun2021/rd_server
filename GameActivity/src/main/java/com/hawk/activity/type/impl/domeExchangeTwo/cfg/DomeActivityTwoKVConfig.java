package com.hawk.activity.type.impl.domeExchangeTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 策划要求目录如此. 就是rose
 * @author jm
 *
 */
@HawkConfigManager.KVResource(file = "activity/rose/rose_exchange_cfg.xml")
public class DomeActivityTwoKVConfig extends HawkConfigBase {
	
	private final int serverDelay;
	
	public DomeActivityTwoKVConfig(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
}
