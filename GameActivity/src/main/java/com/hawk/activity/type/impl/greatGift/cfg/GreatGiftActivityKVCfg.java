package com.hawk.activity.type.impl.greatGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/super_gift/super_gift_activity_cfg.xml")
public class GreatGiftActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 是否开启对于阶段购买顺序(如果开启，则只能按照阶段顺序购买) **/
	private final int isOpen; 
	
	private static GreatGiftActivityKVCfg instance;
	
	public static GreatGiftActivityKVCfg getInstance(){
		return instance;
	}

	public GreatGiftActivityKVCfg() {
		serverDelay = 0;
		isOpen = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public boolean isOpenGradeBuy(){
		return isOpen > 0;
	}
}
