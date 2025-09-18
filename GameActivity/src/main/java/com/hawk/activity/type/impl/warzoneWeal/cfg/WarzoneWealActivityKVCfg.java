package com.hawk.activity.type.impl.warzoneWeal.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/warzone_weal/warzone_weal_activity_cfg.xml")
public class WarzoneWealActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 是否跨天重置(配1则每天零点重置成就) */
	private final int dailyRefresh;

	public WarzoneWealActivityKVCfg() {
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
