package com.hawk.activity.type.impl.healexchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**勋章宝藏kv配置表
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/heal_exchange/heal_exchange_cfg.xml")
public class HealExchangeActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public HealExchangeActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}