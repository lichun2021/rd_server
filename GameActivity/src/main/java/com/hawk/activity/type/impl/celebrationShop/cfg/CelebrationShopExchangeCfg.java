package com.hawk.activity.type.impl.celebrationShop.cfg;

import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author luke
 */
@HawkConfigManager.XmlResource(file = "activity/celebration_shop/celebration_shop_exchange.xml")
public class CelebrationShopExchangeCfg extends AExchangeTipConfig {
	/** 成就id */
	@Id
	private final int id;
	private final String pay;
	private final String gain;
	private final int exchangeCount;

	public CelebrationShopExchangeCfg() {
		id = 0;
		exchangeCount = 0;
		pay="";
		gain="";
	}

	public int getId() {
		return id;
	}

	public String getPay() {
		return pay;
	}

	public String getGain() {
		return gain;
	}

	public int getExchangeCount() {
		return exchangeCount;
	}

}

