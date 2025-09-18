package com.hawk.activity.type.impl.emptyModelThree.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动3 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/empty_model_three/empty_model_three_activity_cfg.xml")
public class EmptyModelThreeActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	private final long resetTime;
	public EmptyModelThreeActivityKVCfg() {
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
