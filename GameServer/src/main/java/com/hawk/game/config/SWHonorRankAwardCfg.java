package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/sw_honor_rank_award.xml")
public class SWHonorRankAwardCfg extends HawkConfigBase {
	// <data id="1" round="1" rankMin="1" rankMax="1" award="30000_500202_2,30000_1300017_20,30000_500301_5,30000_500302_5" />
	@Id
	private final int id;

	private final int round;

	private final int rankMin;
	private final int rankMax;

	private final String award;

	/**
	 * 构造
	 */
	public SWHonorRankAwardCfg() {
		id = 0;
		round = 0;
		rankMin = 0;
		rankMax = 0;
		award = "";

	}

	public int getId() {
		return id;
	}

	public int getRound() {
		return round;
	}

	public int getRankMin() {
		return rankMin;
	}

	public int getRankMax() {
		return rankMax;
	}

	public String getAward() {
		return award;
	}

}
