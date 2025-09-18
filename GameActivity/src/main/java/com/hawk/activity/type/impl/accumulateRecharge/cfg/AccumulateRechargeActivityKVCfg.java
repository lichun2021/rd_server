package com.hawk.activity.type.impl.accumulateRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 累计充值活动配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/accumulate_recharge_achieve/recharge_activity_cfg.xml")
public class AccumulateRechargeActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public AccumulateRechargeActivityKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}
