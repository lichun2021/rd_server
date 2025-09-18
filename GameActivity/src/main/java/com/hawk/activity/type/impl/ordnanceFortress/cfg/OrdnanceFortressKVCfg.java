package com.hawk.activity.type.impl.ordnanceFortress.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制礼包活动配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/ordnance_fortress/ordnance_fortress_cfg.xml")
public class OrdnanceFortressKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final String costItem;
	
	


	

	public OrdnanceFortressKVCfg() {
		serverDelay = 0;
		costItem = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getCostItem() {
		return costItem;
	}

	
	
	
}
