package com.hawk.activity.type.impl.groupBuy.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/group_buy/group_buying_cfg.xml")
public class GroupBuyActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	
	public GroupBuyActivityKVCfg() {
		this.serverDelay = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

}
