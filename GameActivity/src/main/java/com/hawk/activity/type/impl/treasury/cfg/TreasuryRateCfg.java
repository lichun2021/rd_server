package com.hawk.activity.type.impl.treasury.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/treasury/%s/activity_treasury_rate.xml", autoLoad=false, loadParams="44")
public class TreasuryRateCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 区间最小值，不包含最小值
	 */
	private final int min;
	/**
	 * 区间最大值
	 */
	private final int max;
	/**
	 * 比率
	 */
	private final int rate;
	
	public TreasuryRateCfg() {
		this.id = 0;
		this.min = 0;
		this.max = 0;
		this.rate = 0;
	}
	
	public int getId() {
		return id;
	}
	public int getMin() {
		return min;
	}
	public int getMax() {
		return max;
	}
	public int getRate() {
		return rate;
	}
	
}
