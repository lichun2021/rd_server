package com.hawk.activity.type.impl.customgift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/custom_made/custom_made_activity_cfg.xml")
public class CustomGiftActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public CustomGiftActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
}
