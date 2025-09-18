package com.hawk.activity.type.impl.emptyModelSeven.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动7 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/empty_model_seven/empty_model_seven_activity_cfg.xml")
public class EmptyModelSevenActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public EmptyModelSevenActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
