package com.hawk.activity.type.impl.timeLimitDrop.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/time_limit_drop/time_limit_drop_cfg.xml")
public class TimeLimitDropCfg extends HawkConfigBase {
	/**
	 * 服务器的时间
	 */
	private final long serverDelay;
	/**
	 * 排行榜的人数
	 */
	private final int rankSize;
	/**
	 * 物品的id
	 */
	private final int itemId;
	private static TimeLimitDropCfg instance;
	
	public static TimeLimitDropCfg getInstance() {
		return instance; 
	}
	
	public TimeLimitDropCfg(){
		serverDelay = 0l;
		rankSize = 0;
		itemId = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getRankSize() {
		return rankSize;
	}

	public int getItemId() {
		return itemId;
	}
}
