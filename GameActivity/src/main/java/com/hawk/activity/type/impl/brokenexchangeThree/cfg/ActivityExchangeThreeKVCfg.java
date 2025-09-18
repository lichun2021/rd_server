package com.hawk.activity.type.impl.brokenexchangeThree.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战略储备活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/exchange_three/activity_exchange_three_cfg.xml")
public class ActivityExchangeThreeKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public ActivityExchangeThreeKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
