package com.hawk.activity.type.impl.doubleRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 首充双倍重置活动配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/double_recharge/double_recharge_cfg.xml")
public class DoubleRechargeActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public DoubleRechargeActivityKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}
