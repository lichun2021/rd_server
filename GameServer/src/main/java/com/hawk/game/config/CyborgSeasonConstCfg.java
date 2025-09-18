package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 赛博之战基础配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "xml/cyborg_season_const.xml")
public class CyborgSeasonConstCfg extends HawkConfigBase {
	
	
	/**
	 * 实例
	 */
	private static CyborgSeasonConstCfg instance = null;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static CyborgSeasonConstCfg getInstance() {
		return instance;
	}

	
	private final int pointsTarget;
	
	private final double joinCoefficient;
	
	
	private final double pointsCoefficient;
	
	private final double baseCoefficient;

	

	/**
	 * 构造
	 */
	public CyborgSeasonConstCfg() {
		instance = this;
		pointsTarget = 0;
		joinCoefficient = 0;
		pointsCoefficient = 0;
		baseCoefficient = 0;
	}



	public int getPointsTarget() {
		return pointsTarget;
	}



	public double getJoinCoefficient() {
		return joinCoefficient;
	}



	public double getPointsCoefficient() {
		return pointsCoefficient;
	}
	
	public double getBaseCoefficient() {
		return baseCoefficient;
	}
}
