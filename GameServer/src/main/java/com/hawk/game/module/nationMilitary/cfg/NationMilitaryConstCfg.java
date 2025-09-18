package com.hawk.game.module.nationMilitary.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/nation_military_const.xml")
public class NationMilitaryConstCfg extends HawkConfigBase {
	// # 最大军功值，达到此值不再增加
	private final int max;// = 100000

	// # 每一期航海远征，战斗可获得的最大军功值
	private final int crossMax;// = 2000

	public NationMilitaryConstCfg() {
		max = 10000;
		crossMax = 2000;
	}

	public int getMax() {
		return max;
	}

	public int getCrossMax() {
		return crossMax;
	}

}
