package com.hawk.activity.type.impl.redPackage.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/red_packet/red_packet_activity_cfg.xml")
public class RedPackageKVCfg extends HawkConfigBase {
	
	private final long serverDelay;
	
	private final int getIntegral;
	
	private final String reward;
	
	private final int checkNum;
	
	public RedPackageKVCfg(){
		this.serverDelay = 0;
		getIntegral = 0;
		reward = "";
		checkNum = 30;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getGetIntegral() {
		return getIntegral;
	}

	public String getReward() {
		return reward;
	}

	public int getCheckNum() {
		return checkNum;
	}
	
	
	
	
}
