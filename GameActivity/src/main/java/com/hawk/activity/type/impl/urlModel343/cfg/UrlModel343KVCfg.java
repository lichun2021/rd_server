package com.hawk.activity.type.impl.urlModel343.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/url_model_343/url_model_343_cfg.xml")
public class UrlModel343KVCfg extends URLRewardBaseCfg {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	private final String serverOpenTime;
	
	public UrlModel343KVCfg() {
		serverDelay = 0;
		serverOpenTime = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getServerOpenTime() {
		return serverOpenTime;
	}
	
}
