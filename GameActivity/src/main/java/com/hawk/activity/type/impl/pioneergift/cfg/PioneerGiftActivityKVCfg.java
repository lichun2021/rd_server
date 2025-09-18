package com.hawk.activity.type.impl.pioneergift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 先锋豪礼活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/pioneer_gift/pioneer_gift_activity_cfg.xml")
public class PioneerGiftActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**
	 * 免费礼包
	 */
	private final String freeGift;

	public PioneerGiftActivityKVCfg() {
		serverDelay = 0;
		freeGift = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getFreeGift() {
		return freeGift;
	}
	
}
