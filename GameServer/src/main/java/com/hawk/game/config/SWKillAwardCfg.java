package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/sw_kill_award.xml")
public class SWKillAwardCfg extends HawkConfigBase {
	// <data id="1" round="1" target="1000" award="30000_500202_2,30000_1300017_20,30000_500301_5,30000_500302_5" />
	@Id
	private final int id;

	private final int round;

	private final int target;

	private final String award;

	/**
	 * 构造
	 */
	public SWKillAwardCfg() {
		id = 0;
		round = 0;
		target = 0;
		award = "";

	}

	public int getId() {
		return id;
	}

	public int getRound() {
		return round;
	}

	public int getTarget() {
		return target;
	}

	public String getAward() {
		return award;
	}

}
