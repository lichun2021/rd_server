package com.hawk.activity.type.impl.yuriAchieveShowTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 辐射危机2活动K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/yuri_achieve_show_two/yuri_show_two_cfg.xml")
public class YuriAchieveShowTwoActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	public YuriAchieveShowTwoActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}