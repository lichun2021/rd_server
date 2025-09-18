package com.hawk.activity.type.impl.redEnvelope.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/redPacket_achieve/redPacket_activity_cfg.xml")
public class RedEnvelopeKVCfg extends HawkConfigBase {
	
	private final long serverDelay;
	
	public RedEnvelopeKVCfg(){
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
}
