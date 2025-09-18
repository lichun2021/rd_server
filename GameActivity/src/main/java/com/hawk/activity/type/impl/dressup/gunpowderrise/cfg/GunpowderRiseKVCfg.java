package com.hawk.activity.type.impl.dressup.gunpowderrise.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/dress_smoke_again/dress_smoke_again_cfg.xml")
public class GunpowderRiseKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;

	
	public GunpowderRiseKVCfg(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
}