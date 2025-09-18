package com.hawk.activity.type.impl.continuousRecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/even_filling/even_filling_activity_cfg.xml")
public class ContinuousRechargeActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public ContinuousRechargeActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
