package com.hawk.activity.type.impl.resourceDefense.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 资源保卫战等级配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_level.xml")
public class ResourceDefenseLevelCfg extends HawkConfigBase {

	@Id
	private final int level;
	
	/**
	 * 经验
	 */
	private final int exp;
	
	/**
	 * 产量/s
	 */
	private final int yield;
	
	/**
	 * 偷取成功概率
	 */
	private final int pickupWeight;
	
	/**
	 * 偷取比例
	 */
	private final int pickupPoint;
	
	public ResourceDefenseLevelCfg() {
		level= 0;
		exp = 0;
		yield = 0;
		pickupWeight = 0;
		pickupPoint = 0;
	}

	public int getLevel() {
		return level;
	}

	public int getExp() {
		return exp;
	}

	public int getYield() {
		return yield;
	}

	public int getPickupWeight() {
		return pickupWeight;
	}

	public int getPickupPoint() {
		return pickupPoint;
	}
}
