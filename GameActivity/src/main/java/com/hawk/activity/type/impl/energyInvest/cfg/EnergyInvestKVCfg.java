package com.hawk.activity.type.impl.energyInvest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/power_invest/power_invest_cfg.xml")
public class EnergyInvestKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	public EnergyInvestKVCfg(){
		serverDelay = 0;
		
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
