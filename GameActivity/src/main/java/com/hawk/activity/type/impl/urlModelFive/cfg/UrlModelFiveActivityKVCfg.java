package com.hawk.activity.type.impl.urlModelFive.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动5 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_five/url_model_five_cfg.xml")
public class UrlModelFiveActivityKVCfg extends URLRewardBaseCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModelFiveActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
