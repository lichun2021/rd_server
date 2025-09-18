package com.hawk.activity.type.impl.monster2.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 13号活动加入endDate字段,在这个时间后开服的新服不在触发活动
 */
@HawkConfigManager.KVResource(file = "activity/yuri_achieve/yuri_achieve_cfg.xml")
public class YuriAchieveActivityKVCfg extends HawkConfigBase {
	
	
	private final String endDate;
	
	public YuriAchieveActivityKVCfg() {
		endDate = "";
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public long getEndDateTime() {
		return HawkTime.parseTime(endDate);
	}
}