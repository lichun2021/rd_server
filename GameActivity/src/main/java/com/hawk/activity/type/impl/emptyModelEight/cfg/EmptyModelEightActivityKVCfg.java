package com.hawk.activity.type.impl.emptyModelEight.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动8 KV配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/first_recharge_reset/first_recharge_reset_cfg.xml")
public class EmptyModelEightActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public EmptyModelEightActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
