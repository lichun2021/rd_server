package com.hawk.activity.type.impl.supersoldierInvest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/mecha_invest/mecha_invest_cfg.xml")
public class SupersoldierInvestKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	public SupersoldierInvestKVCfg(){
		serverDelay = 0;
		
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
