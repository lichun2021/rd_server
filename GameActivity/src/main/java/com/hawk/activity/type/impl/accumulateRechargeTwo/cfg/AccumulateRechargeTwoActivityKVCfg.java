package com.hawk.activity.type.impl.accumulateRechargeTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 累计充值活动2配置
 */
@HawkConfigManager.KVResource(file = "activity/daily_recharge/%s/daily_recharge_activity_cfg.xml", autoLoad=false, loadParams="140")
public class AccumulateRechargeTwoActivityKVCfg extends HawkConfigBase {
	/** 是否跨天重置(配1则每天零点重置成就) */
	private final int isDailyReset;


	public AccumulateRechargeTwoActivityKVCfg() {
		isDailyReset = 0;
	}

	public boolean isDailyRefresh() {
		return isDailyReset == 1;
	}
}
