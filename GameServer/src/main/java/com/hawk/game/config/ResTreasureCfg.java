package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 资源宝库
 *
 */
@HawkConfigManager.XmlResource(file = "xml/res_treasure.xml")
public class ResTreasureCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 资源总量
	protected final String totalRes;
	// 首次搬运奖励id，在award表配置
	protected final int firstAward;
	// 存活周期
	protected final int lifeTime;
	// 半径
	protected final int radius;
	// 最大采集负重
	protected final int weight;

	public ResTreasureCfg() {
		id = 0;
		totalRes = "";
		lifeTime = 0;
		firstAward = 0;
		radius = 2;
		weight = 1000000;
	}

	public int getId() {
		return id;
	}

	public String getTotalRes() {
		return totalRes;
	}

	public int getFirstAward() {
		return firstAward;
	}

	public int getLifeTime() {
		return lifeTime;
	}

	public int getRadius() {
		return radius;
	}

	public int getWeight() {
		return weight;
	}

}