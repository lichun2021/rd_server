package com.hawk.activity.type.impl.newyearTreasure.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/newyear_treasure/newyear_treasure_cfg.xml")
public class NewyearTreasureActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	public NewyearTreasureActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
