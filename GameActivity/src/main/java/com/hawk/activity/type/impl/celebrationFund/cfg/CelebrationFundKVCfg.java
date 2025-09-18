package com.hawk.activity.type.impl.celebrationFund.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 庆典基金
 */
@HawkConfigManager.KVResource(file = "activity/celebration_fund/celebration_fund_kv_cfg.xml")
public class CelebrationFundKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 距离活动结束前X秒可购买积分
	 */
	private final int buyPointTimeStart;
	/**
	 * 距离活动结束前X秒不可再购买积分
	 */
	private final int buyPointTimeEnd;

	public CelebrationFundKVCfg() {
		serverDelay = 0;
		buyPointTimeStart = 0;
		buyPointTimeEnd = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public long getBuyPointTimeStartTime() {
		return buyPointTimeStart * 1000L;
	}
	
	public long getBuyPointTimeEndTime() {
		return buyPointTimeEnd * 1000L;
	}
}
