package com.hawk.activity.type.impl.doubleGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 双享豪礼活动配置
 */
@HawkConfigManager.KVResource(file = "activity/double_gift/double_gift_cfg.xml")
public class DoubleGiftActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public DoubleGiftActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
}
