package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

@HawkConfigManager.XmlResource(file = "xml/dyzz_rogue_armsusability_weight.xml")
public class DYZZRogueArmsusabilityWeightCfg extends HawkConfigBase {
	@Id
	private final int id;// ="1"
	private final int weight;// ="1"

	public DYZZRogueArmsusabilityWeightCfg() {
		id = 0;
		weight = 10;
	}

	@Override
	protected boolean assemble() {
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		HawkAssert.isTrue(weight > 0);
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

}
