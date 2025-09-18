package com.hawk.activity.type.impl.drogenBoatFestival.exchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/dw_exchange/dw_exchange_cfg.xml")
public class DragonBoatExchangeKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;

	public DragonBoatExchangeKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	
	
}
