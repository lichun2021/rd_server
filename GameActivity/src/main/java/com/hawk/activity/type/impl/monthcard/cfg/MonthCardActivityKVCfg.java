package com.hawk.activity.type.impl.monthcard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 月卡活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/month_card/activity_monthCard_cfg.xml")
public class MonthCardActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	public MonthCardActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}