package com.hawk.activity.type.impl.drogenBoatFestival.exchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典 等级配置
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/dw_exchange/dw_exchange_exchange.xml")
public class DragonBoatExchangeCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应礼包ID
	private final String pay;
	// 奖励内容
	private final String gain;
	
	private final int exchangeCount;

	public DragonBoatExchangeCfg() {
		id = 0;
		pay = "";
		gain = "";
		exchangeCount = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
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
