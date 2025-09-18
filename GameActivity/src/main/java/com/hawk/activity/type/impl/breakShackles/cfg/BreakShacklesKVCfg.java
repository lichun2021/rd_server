package com.hawk.activity.type.impl.breakShackles.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *冲破枷锁活动K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/break_shackles/break_shackles_cfg.xml")
public class BreakShacklesKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;

	
	public BreakShacklesKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	
}