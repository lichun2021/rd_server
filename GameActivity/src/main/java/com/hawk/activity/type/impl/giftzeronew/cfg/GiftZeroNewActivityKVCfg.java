package com.hawk.activity.type.impl.giftzeronew.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 新0元礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/new_gift_zero/%s/new_gift_zero_cfg.xml", autoLoad=false, loadParams="262")
public class GiftZeroNewActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 每日免费奖励
	 */
	private final String reward;

	public GiftZeroNewActivityKVCfg() {
		serverDelay = 0;
		reward = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getReward() {
		return reward;
	}

}
