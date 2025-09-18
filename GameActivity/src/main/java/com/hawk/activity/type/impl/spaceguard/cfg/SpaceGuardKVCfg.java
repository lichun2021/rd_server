package com.hawk.activity.type.impl.spaceguard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/space_machine_guard/space_machine_guard_cfg.xml")
public class SpaceGuardKVCfg extends HawkConfigBase {
	
	// 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	public SpaceGuardKVCfg(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

}
