package com.hawk.activity.type.impl.midAutumn.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战略储备活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/mid_autumn/mid_autumn_activity_cfg.xml")
public class MidAutumnActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public MidAutumnActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
