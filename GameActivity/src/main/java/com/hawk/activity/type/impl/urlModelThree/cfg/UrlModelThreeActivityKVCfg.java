package com.hawk.activity.type.impl.urlModelThree.cfg;

import com.hawk.activity.type.impl.urlReward.URLServerOpenCfg;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动3 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_three/url_model_three_cfg.xml")
public class UrlModelThreeActivityKVCfg extends URLServerOpenCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModelThreeActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
