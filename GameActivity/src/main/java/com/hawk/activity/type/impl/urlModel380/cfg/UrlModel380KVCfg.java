package com.hawk.activity.type.impl.urlModel380.cfg;

import com.hawk.activity.type.impl.urlReward.URLServerOpenCfg;
import org.hawk.config.HawkConfigManager;

/**
 * url模板活动2 KV配置
 * @author zhy
 *
 */
@HawkConfigManager.KVResource(file = "activity/act_380/act_380_cfg.xml")
public class UrlModel380KVCfg extends URLServerOpenCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public UrlModel380KVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
