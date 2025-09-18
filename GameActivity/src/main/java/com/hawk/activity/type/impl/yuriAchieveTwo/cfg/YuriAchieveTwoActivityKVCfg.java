package com.hawk.activity.type.impl.yuriAchieveTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 使命战争2活动K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/yuri_achieve_two/yuri_achieve_two_activity_cfg.xml")
public class YuriAchieveTwoActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	public YuriAchieveTwoActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}