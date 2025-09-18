package com.hawk.activity.type.impl.luckyWelfare.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/lucky_welfare/lucky_welfare_cfg.xml")
public class LuckyWelfareActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 是否跨天重置(配1则每天零点重置成就) */
	private final int dailyRefresh;

	public LuckyWelfareActivityKVCfg() {
		dailyRefresh = 0;
		serverDelay = 0;
	}

	public boolean isDailyRefresh() {
		return dailyRefresh == 1;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}
