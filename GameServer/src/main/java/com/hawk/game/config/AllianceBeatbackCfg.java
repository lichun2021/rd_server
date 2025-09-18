package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/alliance_beatback.xml")
public class AllianceBeatbackCfg extends HawkConfigBase {
	protected final int id;// ="1"
	protected final int power;// ="5000" 死多少发启
	protected final int killPower;// ="5000" 杀回来多少达成
	protected final String systemMoney;// ="10000_1001_10"

	public AllianceBeatbackCfg() {
		id = 0;
		power = 5000;
		killPower = 5000;
		systemMoney = "";
	}

	public int getId() {
		return id;
	}

	public int getPower() {
		return power;
	}

	public int getKillPower() {
		return killPower;
	}

	public String getSystemMoney() {
		return systemMoney;
	}

}
