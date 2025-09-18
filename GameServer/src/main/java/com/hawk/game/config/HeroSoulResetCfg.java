package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/hero_soul_reset.xml")
@HawkConfigBase.CombineId(fields = { "stage", "level" })
public class HeroSoulResetCfg extends HawkConfigBase {
	protected final int id;// ="1"
	protected final int stage;// ="0"
	protected final int level;// ="1"
	protected final String consumption;// ="10000_1001_100"
	protected final String returnItem;// ="30000_1000005_1"

	public HeroSoulResetCfg() {
		this.id = 0;
		this.stage = 1;
		this.level = 1;
		this.consumption = "";
		this.returnItem = "";
	}

	@Override
	protected boolean assemble() {

		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getStage() {
		return stage;
	}

	public int getLevel() {
		return level;
	}

	public String getConsumption() {
		return consumption;
	}

	public String getReturnItem() {
		return returnItem;
	}

}
