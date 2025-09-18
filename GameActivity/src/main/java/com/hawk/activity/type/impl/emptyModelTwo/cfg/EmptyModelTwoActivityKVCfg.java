package com.hawk.activity.type.impl.emptyModelTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模板活动2 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/empty_model_two/empty_model_two_activity_cfg.xml")
public class EmptyModelTwoActivityKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public EmptyModelTwoActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
