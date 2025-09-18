package com.hawk.activity.type.impl.urlModelFour.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动4 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_four/url_model_four_cfg.xml")
public class UrlModelFourActivityKVCfg extends URLRewardBaseCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModelFourActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
