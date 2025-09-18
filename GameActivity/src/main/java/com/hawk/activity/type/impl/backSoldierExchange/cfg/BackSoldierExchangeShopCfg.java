package com.hawk.activity.type.impl.backSoldierExchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/back_soldier_exchange/back_soldier_exchange_shop.xml")
public class BackSoldierExchangeShopCfg extends HawkConfigBase {
	// <data id="1" soldierType="1" pay="10000_1000_200" gain="30000_10000066_1" exchangeCount="1" />
	@Id
	private final int id;
	private final int soldierType;
	private final String pay;
	private final String gain;
	private final int exchangeCount;

	public BackSoldierExchangeShopCfg() {
		id = 0;
		soldierType = 0;
		exchangeCount = 0;
		pay = "";
		gain = "";
	}

	public int getId() {
		return id;
	}

	public int getSoldierType() {
		return soldierType;
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
