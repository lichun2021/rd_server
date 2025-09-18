package com.hawk.activity.type.impl.immgration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class ImmgrationLimitCfg extends HawkConfigBase {
	
	@Id
	private final int id;

	/**
	 * 排名
	 */
	private final int rank;
	
	/**
	 * 人数限制
	 */
	private final int number;
	
	/**
	 * 战力限制
	 */
	private final int power;

	private final int powerMin;
	
	public ImmgrationLimitCfg() {
		id = 0;
		rank = 0;
		number = 0;
		power = 0;
		powerMin = 0;
	}

	public int getId() {
		return id;
	}

	public int getRank() {
		return rank;
	}

	public int getNum() {
		return number;
	}

	public int getPower() {
		return power;
	}

	public int getPowerMin() {
		return powerMin;
	}
}
