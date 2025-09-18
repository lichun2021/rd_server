package com.hawk.activity.type.impl.rechargeFund.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.KVResource;

@KVResource(file = "activity/recharge_fund/recharge_fund_cfg.xml")
public class RechargeFundKVCfg extends HawkConfigBase {
	private final int serverDelay;

	/**
	 * 投资期间持续时间；单位：秒
	 */
	private final long buyTime;

	public RechargeFundKVCfg() {
		this.serverDelay = 0;
		this.buyTime = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getBuyTime() {
		return buyTime * 1000l;
	}

}
