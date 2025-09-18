package com.hawk.activity.type.impl.buildlevel.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 基地崛起活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/build_level/%s/build_level_activity_cfg.xml", autoLoad=false, loadParams="6")
public class BuildLevelActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	private final long resetTime;
	public BuildLevelActivityKVCfg() {
		serverDelay = 0;
		resetTime = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getResetTime() {
		return resetTime * 1000l;
	}

}
