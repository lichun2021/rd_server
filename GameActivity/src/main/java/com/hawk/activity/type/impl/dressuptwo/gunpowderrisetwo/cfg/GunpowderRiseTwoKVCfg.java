package com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 圣诞节系列活动三:冰雪商城活动
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/christmas_snow_shop/christmas_snow_shop_cfg.xml")
public class GunpowderRiseTwoKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;

	
	public GunpowderRiseTwoKVCfg(){
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
}