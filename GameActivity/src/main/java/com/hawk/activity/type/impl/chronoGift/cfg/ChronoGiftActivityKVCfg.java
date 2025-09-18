package com.hawk.activity.type.impl.chronoGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制礼包活动配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/space_gift/space_gift_cfg.xml")
public class ChronoGiftActivityKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;

	//时空之门解锁道具
	private final String openCost;
	
	//时空之门解锁道具购买金币单价
	private final String itemPrice;
	
	//每日购买个数上限
	private final int buyLimit;

	public ChronoGiftActivityKVCfg() {
		serverDelay = 0;
		openCost = "";
		itemPrice = "";
		buyLimit = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getOpenCost() {
		return openCost;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public int getBuyLimit() {
		return buyLimit;
	}
	
	
	
	
}
