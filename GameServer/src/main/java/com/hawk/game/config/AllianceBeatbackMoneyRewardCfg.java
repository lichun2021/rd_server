package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/alliance_beatbackMoneyReward.xml")
public class AllianceBeatbackMoneyRewardCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="1"
	protected final String costMoney;// ="10000_1000_1"
	protected final String addMoney;// ="10000_1001_1"

	public AllianceBeatbackMoneyRewardCfg() {
		id = 0;
		costMoney = "";
		addMoney = "";
	}

	public int getId() {
		return id;
	}

	public String getCostMoney() {
		return costMoney;
	}

	public String getAddMoney() {
		return addMoney;
	}
}
