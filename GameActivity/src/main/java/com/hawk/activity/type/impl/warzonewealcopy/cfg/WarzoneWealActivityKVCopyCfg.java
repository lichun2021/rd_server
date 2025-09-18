package com.hawk.activity.type.impl.warzonewealcopy.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/gift_send/%s/gift_send_activity_cfg.xml", autoLoad=false, loadParams="95")
public class WarzoneWealActivityKVCopyCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 是否跨天重置(配1则每天零点重置成就) */
	private final int dailyRefresh;

	public WarzoneWealActivityKVCopyCfg() {
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
