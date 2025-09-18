package com.hawk.activity.type.impl.radiationWar.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 新版辐射战争2活动K-V配置
 */
@HawkConfigManager.KVResource(file = "activity/radiation_war/%s/radiation_war_cfg.xml", autoLoad=false, loadParams="184")
public class RadiationWarActivityKVCfg extends HawkConfigBase {
	
	//** 服务器开服延时开启活动时间*//*
	private final int serverDelay;
	
	private final String startDate;
	
	public RadiationWarActivityKVCfg() {
		serverDelay = 0;
		startDate = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getStartDate() {
		return startDate;
	}
	
	public long getStartDateTime() {
		return HawkTime.parseTime(startDate);
	}
}