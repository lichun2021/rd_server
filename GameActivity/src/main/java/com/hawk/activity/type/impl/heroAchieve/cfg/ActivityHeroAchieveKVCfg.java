package com.hawk.activity.type.impl.heroAchieve.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 英雄军团活动配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/hero_achieve/%s/hero_achieve_cfg.xml", autoLoad=false, loadParams="29")
public class ActivityHeroAchieveKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	private final long resetTime;
	public ActivityHeroAchieveKVCfg() {
		serverDelay = 0;
		resetTime = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getResetTime() {
		return resetTime * 1000l;
	}

}
