package com.hawk.activity.type.impl.onermbpurchase.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 一元购活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/one_rmb/one_rmb_cfg.xml")
public class OneRMBPurchaseActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 一期活动首次购买的额外奖励内容
	 */
    private final String firstBuyRewards;
    
	public OneRMBPurchaseActivityKVCfg() {
		serverDelay = 0;
		firstBuyRewards = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getFirstBuyRewards() {
		return firstBuyRewards;
	}
	
}
