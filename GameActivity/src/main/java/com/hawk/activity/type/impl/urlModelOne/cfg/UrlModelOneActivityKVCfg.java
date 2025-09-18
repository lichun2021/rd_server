package com.hawk.activity.type.impl.urlModelOne.cfg;

import com.hawk.activity.type.impl.urlReward.URLServerOpenCfg;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动1 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_one/url_model_one_cfg.xml")
public class UrlModelOneActivityKVCfg extends URLServerOpenCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModelOneActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
