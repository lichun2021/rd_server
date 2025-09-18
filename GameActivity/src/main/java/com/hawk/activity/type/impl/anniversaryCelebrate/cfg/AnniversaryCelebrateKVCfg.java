package com.hawk.activity.type.impl.anniversaryCelebrate.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/anniversary_celebrate/anniversary_celebrate_cfg.xml")
public class AnniversaryCelebrateKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public AnniversaryCelebrateKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
