package com.hawk.activity.type.impl.dressup.drawingsearch.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装扮投放系列活动一:搜寻图纸
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/dress_drawing_search/dress_drawing_search_cfg.xml")
public class DrawingSearchKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 活动期间总掉落数量
	 */
	private final int totalDropLimit;

	public DrawingSearchKVCfg() {
		serverDelay = 0;
		totalDropLimit = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public int getTotalDropLimit() {
		return totalDropLimit;
	}
}
