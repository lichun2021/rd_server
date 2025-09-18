package com.hawk.activity.type.impl.yurirevenge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/yuri_revenge/yuri_revenge_activity_cfg.xml")
public class YuriRevengeActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	/** 尤里复仇战斗可开启时间 单位:s*/
	private final long opentime;
	
	public YuriRevengeActivityKVCfg() {
		serverDelay = 0;
		opentime = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getOpentime() {
		return opentime * 1000l;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
