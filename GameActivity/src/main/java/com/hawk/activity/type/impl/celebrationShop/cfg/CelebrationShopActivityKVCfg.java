package com.hawk.activity.type.impl.celebrationShop.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 配置
 * @author luke
 *
 */
@HawkConfigManager.KVResource(file = "activity/celebration_shop/celebration_shop_cfg.xml")
public class CelebrationShopActivityKVCfg extends HawkConfigBase {
	//服务器开服延时开启活动时间
	private final int serverDelay;

	/**
	 * 分享获得奖励
	 */
	private final String exchangeItemShow;
	
	public CelebrationShopActivityKVCfg() {
		serverDelay = 0;
		exchangeItemShow="";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getExchangeItemShow() {
		return exchangeItemShow;
	}
	
}
