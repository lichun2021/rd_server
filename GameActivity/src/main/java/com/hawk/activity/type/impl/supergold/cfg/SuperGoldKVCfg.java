package com.hawk.activity.type.impl.supergold.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/super_gold/super_gold_cfg.xml")
public class SuperGoldKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 第一次挖矿的固定奖励  **/
	private final String extReward;

	public SuperGoldKVCfg() {
		serverDelay = 0;
		extReward = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getExtReward() {
		return extReward;
	}
}
