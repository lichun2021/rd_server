package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.util.WeightAble;

@HawkConfigManager.XmlResource(file = "xml/alliance_storehouse_random.xml")
public class AllianceStorehouseCfg extends HawkConfigBase implements WeightAble {
	@Id
	protected final int id;
	protected final String name;
	protected final int groupId;
	protected final int weight;
	protected final String award;// "30000_800100_1,30000_850048_1,30000_820001_1"
	protected final String helpaward;// "30000_800100_1,30000_820001_1" />
	protected final String color;// #ffff00
	protected final int levelMin;// ="1"
	protected final int levelMax;// ="30"

	public AllianceStorehouseCfg() {
		id = 0;
		name = "";
		groupId = 0;
		weight = 0;
		award = "";
		helpaward = "";
		color = "";
		levelMin = 1;
		levelMax = 30;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getGroupId() {
		return groupId;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getAward() {
		return award;
	}

	public String getHelpaward() {
		return helpaward;
	}

	public String getColor() {
		return color;
	}

	public int getLevelMin() {
		return levelMin;
	}

	public int getLevelMax() {
		return levelMax;
	}

}
