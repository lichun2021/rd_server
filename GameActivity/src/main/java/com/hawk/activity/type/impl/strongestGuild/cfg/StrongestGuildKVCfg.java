package com.hawk.activity.type.impl.strongestGuild.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 最强指挥官活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/strongest_alliance/activity_strongest_allian_cfg.xml")
public class StrongestGuildKVCfg extends HawkConfigBase {
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	private final int cycleRankId;
	
	private final int cycleAllianceRankId;
	
	private static StrongestGuildKVCfg instance;
	
	public static StrongestGuildKVCfg getInstance(){
		return instance;
	}
	
	public StrongestGuildKVCfg() {
		serverDelay = 0;
		cycleRankId = 0;
		cycleAllianceRankId = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getCycleAllianceRankId() {
		return cycleAllianceRankId;
	}

	public int getCycleRankId() {
		return cycleRankId;
	}
}