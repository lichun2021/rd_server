package com.hawk.activity.type.impl.urlModelTwo.cfg;

import com.hawk.activity.type.impl.urlReward.URLServerOpenCfg;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动2 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_two/url_model_two_cfg.xml")
public class UrlModelTwoActivityKVCfg extends URLServerOpenCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModelTwoActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
