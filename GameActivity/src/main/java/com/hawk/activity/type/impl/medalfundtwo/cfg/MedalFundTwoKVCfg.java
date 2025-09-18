package com.hawk.activity.type.impl.medalfundtwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/medal_invest_two/medal_invest_two_cfg.xml")
public class MedalFundTwoKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	public MedalFundTwoKVCfg(){
		serverDelay = 0;
		
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
