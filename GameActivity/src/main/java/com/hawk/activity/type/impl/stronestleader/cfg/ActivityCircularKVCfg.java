package com.hawk.activity.type.impl.stronestleader.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 最强指挥官活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/strongest_leader/activity_circular_cfg.xml")
public class ActivityCircularKVCfg extends HawkConfigBase {
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	private final int cycleRankId;
	
	public ActivityCircularKVCfg() {
		serverDelay = 0;
		cycleRankId = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}


	public int getCycleRankId() {
		return cycleRankId;
	}
}