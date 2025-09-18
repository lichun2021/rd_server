package com.hawk.activity.type.impl.fullyArmed.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/fully_armed/fully_armed_cfg.xml")
public class FullyArmedKVCfg extends HawkConfigBase {
	

	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	private final boolean packageIsReset;
	public FullyArmedKVCfg(){
		serverDelay = 0;
		packageIsReset = true;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	
	public boolean getPackageIsReset() {
		return this.packageIsReset;
	}
}
