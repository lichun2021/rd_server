package com.hawk.activity.type.impl.firstRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 首充奖励活动配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/first_recharge/first_recharge_reward_cfg.xml")
public class FirstRechargeActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	private final long resetTime;
	public FirstRechargeActivityKVCfg() {
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
