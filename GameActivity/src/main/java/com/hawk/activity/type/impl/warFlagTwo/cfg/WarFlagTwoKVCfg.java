package com.hawk.activity.type.impl.warFlagTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/flag_bridge/flag_bridge_cfg.xml")
public class WarFlagTwoKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;

	
	public WarFlagTwoKVCfg(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
}