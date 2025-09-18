package com.hawk.game.module.nationMilitary.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.XmlResource(file = "xml/nation_military_rank_time.xml")
public class NationMilitaryRankTimeCfg extends HawkConfigBase {

	@Id
	protected final int id;// ="20"
	protected final String resetTime;// ="2023-4-3 00:00:00"

	private long resetTimeLongValue;

	public NationMilitaryRankTimeCfg() {
		this.id = 101;
		this.resetTime = "2023-4-3 00:00:00";
	}

	@Override
	protected boolean assemble() {
		resetTimeLongValue = HawkTime.parseTime(resetTime);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public String getResetTime() {
		return resetTime;
	}

	public long getResetTimeLongValue() {
		return resetTimeLongValue;
	}

}
