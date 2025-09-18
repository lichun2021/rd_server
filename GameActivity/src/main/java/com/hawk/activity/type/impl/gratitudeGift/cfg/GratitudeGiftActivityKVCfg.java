package com.hawk.activity.type.impl.gratitudeGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 感恩巨献 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/gratitude_gift/gratitude_gift_cfg.xml")
public class GratitudeGiftActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public GratitudeGiftActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
