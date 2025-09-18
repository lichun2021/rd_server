package com.hawk.activity.type.impl.order.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/order/order_cfg.xml")
public class OrderActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	public OrderActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
