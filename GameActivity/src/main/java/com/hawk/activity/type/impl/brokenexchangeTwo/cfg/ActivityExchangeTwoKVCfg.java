package com.hawk.activity.type.impl.brokenexchangeTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战略储备活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/exchange_two/%s/activity_exchange_two_cfg.xml", autoLoad=false, loadParams="61")
public class ActivityExchangeTwoKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public ActivityExchangeTwoKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
