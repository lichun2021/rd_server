package com.hawk.activity.type.impl.equipBlackMarket.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动3 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/arms_market/arms_market_cfg.xml")
public class EquipBlackMarketKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	

	public EquipBlackMarketKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	
}
