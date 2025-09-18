package com.hawk.activity.type.impl.supergoldtwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.KVResource(file = "activity/super_gold_two/%s/super_gold_two_cfg.xml", autoLoad=false, loadParams="210")
public class SuperGoldTwoKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 第一次挖矿的固定奖励  **/
	private final String extReward;
	
	private final String startDate; 

	public SuperGoldTwoKVCfg() {
		serverDelay = 0;
		extReward = "";
		startDate = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getExtReward() {
		return extReward;
	}
	

	public String getStartDate() {
		return startDate;
	}
	
	public long getStartDateTime() {
		return HawkTime.parseTime(startDate);
	}
}
