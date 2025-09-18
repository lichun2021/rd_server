package com.hawk.activity.type.impl.heroLove.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 委任英雄
 * @author jm
 *
 */
@HawkConfigManager.KVResource(file = "activity/hero_love/hero_love_cfg.xml")
public class HeroLoveActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 最大的分数
	 */
	private final int maxScore;

	public HeroLoveActivityKVCfg() {
		serverDelay = 0;
		maxScore = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getMaxScore() {
		return maxScore;
	}	
}
