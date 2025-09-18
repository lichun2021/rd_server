package com.hawk.activity.type.impl.dailyrecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 今日累计充值活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/recharge_buy/recharge_buy_cfg.xml")
public class RechargeBuyActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public RechargeBuyActivityKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}
