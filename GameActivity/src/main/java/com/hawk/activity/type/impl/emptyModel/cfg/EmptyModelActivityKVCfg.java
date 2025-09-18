package com.hawk.activity.type.impl.emptyModel.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/empty_model/empty_model_activity_time_cfg.xml")
public class EmptyModelActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public EmptyModelActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
