package com.hawk.activity.type.impl.peakHonour.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
	
@HawkConfigManager.XmlResource(file = "activity/peak_honour/peak_honour_points_get.xml")
public class PeakHonourPointsGetCfg extends HawkConfigBase {
	
	/**
	 * 获取积分途径
	 * 1:使用一个金条给n点积分
	 * 2:使用一个金币给n点积分
	 * 3:消耗1点体力给n点积分
	 * 4:使用1min加速给n点积分
	 */
	@Id
	private final int getType;
	
	/**
	 * 每1点数量可转换获得的积分数量(*10000后)
	 */
	private final int proportion;
	
	/**
	 * 是否有获取上限
	 */
	private final int isGetLimit;
	
	/**
	 * 获取上限
	 */
	private final int limitPoints;
	
	/**
	 * 构造
	 */
	public PeakHonourPointsGetCfg() {
		getType = 0;
		proportion = 0;
		isGetLimit = 0;
		limitPoints = 0;
	}

	public int getGetType() {
		return getType;
	}

	public int getProportion() {
		return proportion;
	}

	public int getIsGetLimit() {
		return isGetLimit;
	}

	public int getLimitPoints() {
		return limitPoints;
	}
}
