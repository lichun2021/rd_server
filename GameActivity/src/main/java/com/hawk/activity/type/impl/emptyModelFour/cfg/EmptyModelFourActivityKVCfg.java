package com.hawk.activity.type.impl.emptyModelFour.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动4 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/empty_model_four/empty_model_four_activity_cfg.xml")
public class EmptyModelFourActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public EmptyModelFourActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
