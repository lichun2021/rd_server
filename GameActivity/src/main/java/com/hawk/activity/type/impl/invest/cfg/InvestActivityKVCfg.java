package com.hawk.activity.type.impl.invest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 投资理财活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/invest/invest_cfg.xml")
public class InvestActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public InvestActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
}
