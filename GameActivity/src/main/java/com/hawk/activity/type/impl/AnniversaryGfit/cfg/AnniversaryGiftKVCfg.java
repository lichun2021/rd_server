package com.hawk.activity.type.impl.AnniversaryGfit.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 累计消费活动配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/anniversary_gift/anniversary_gift_kv_cfg.xml")
public class AnniversaryGiftKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public AnniversaryGiftKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
