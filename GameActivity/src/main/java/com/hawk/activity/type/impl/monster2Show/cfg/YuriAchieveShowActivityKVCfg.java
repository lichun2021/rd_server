package com.hawk.activity.type.impl.monster2Show.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 18号活动加入endDate字段,在这个时间后开服的新服不在触发活动
 */
@HawkConfigManager.KVResource(file = "activity/yuri_achieve_show/yuri_activity_cfg.xml")
public class YuriAchieveShowActivityKVCfg extends HawkConfigBase {
	
	private final String endDate;
	
	public YuriAchieveShowActivityKVCfg() {
		endDate = "";
	}

	public String getEndDate() {
		return endDate;
	}
	
	public long getEndDateTime() {
		return HawkTime.parseTime(endDate);
	}
	
}