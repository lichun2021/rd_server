package com.hawk.activity.type.impl.healexchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**勋章宝藏成就数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/heal_exchange/heal_exchange.xml")
public class HealExchangeExchangeAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int termId;
	private final int exchangeCount;

	private final String pay;
	private final String gain;

	public HealExchangeExchangeAwardCfg() {
		id = 0;
		termId = 0;
		exchangeCount = 0;
		pay = "";
		gain = "";
	}

	public int getId() {
		return id;
	}

	public int getExchangeCount() {
		return exchangeCount;
	}

	public String getPay() {
		return pay;
	}

	public String getGain() {
		return gain;
	}

	public int getTermId() {
		return termId;
	}

}
