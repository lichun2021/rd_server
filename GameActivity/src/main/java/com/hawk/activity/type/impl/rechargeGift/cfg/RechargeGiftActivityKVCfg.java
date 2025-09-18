package com.hawk.activity.type.impl.rechargeGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/recharge_gift/%s/recharge_gift_cfg.xml", autoLoad=false, loadParams="36")
public class RechargeGiftActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	private final long resetTime;
	public RechargeGiftActivityKVCfg() {
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
