package com.hawk.activity.type.impl.redEnvelopePlayer.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/playerRedPacket/playerRedPacket_activity_cfg.xml")
public class RedEnvelopePlayerKVCfg extends HawkConfigBase {
	
	private final long serverDelay;
	
	private final long overdueTime;
	
	public RedEnvelopePlayerKVCfg(){
		this.serverDelay = 0;
		this.overdueTime = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getOverdueTime() {
		return overdueTime * 1000l;
	}
}
