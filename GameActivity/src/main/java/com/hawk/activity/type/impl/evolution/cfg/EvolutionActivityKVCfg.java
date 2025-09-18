package com.hawk.activity.type.impl.evolution.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


@HawkConfigManager.KVResource(file = "activity/evoroad/evoroad_cfg.xml")
public class EvolutionActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	/** 开服时间晚于此时间的服务器开启  */
	private final String serverOpenTime;
	
	/** 开服时间早于此时间的服务器开启 */
	private final String serverEndOpenTime;
	
	private long serverOpenTimeValue;
	private long serverEndOpenTimeValue;
	
	public EvolutionActivityKVCfg() {
		serverDelay = 0;
		serverOpenTime = "";
		serverEndOpenTime = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
		serverEndOpenTimeValue = HawkTime.parseTime(serverEndOpenTime);
		return true;
	}

	public long getServerOpenTimeValue() {
		return serverOpenTimeValue;
	}

	public long getServerEndOpenTimeValue() {
		return serverEndOpenTimeValue;
	}

}
