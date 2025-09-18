package com.hawk.activity.type.impl.rechargeQixi.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
@HawkConfigManager.KVResource(file = "activity/recharge_qixi/recharge_qixi_cfg.xml")
public class RechargeQixiActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public RechargeQixiActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
