package com.hawk.activity.type.impl.shopSkip.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动3 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/shop_skip/shop_skip_activity_cfg.xml")
public class ShopSkipActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public ShopSkipActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
