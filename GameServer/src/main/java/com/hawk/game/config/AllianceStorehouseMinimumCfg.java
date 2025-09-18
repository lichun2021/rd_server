package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkRandObj;

import com.google.common.base.Splitter;

@HawkConfigManager.XmlResource(file = "xml/alliance_storehouse_minimum.xml")
public class AllianceStorehouseMinimumCfg extends HawkConfigBase {
	@Id
	protected final int id;
	protected final int upgradeNum;// ="2"
	protected final String randomPond;// ="1_1;2_2"
	private List<Gweight> rpList = new ArrayList<>();

	private class Gweight implements HawkRandObj {
		private int groupId;
		private int weight;

		@Override
		public int getWeight() {
			return weight;
		}

	}

	public AllianceStorehouseMinimumCfg() {
		id = 0;
		upgradeNum = 2;
		randomPond = "1_1;2_2";
	}

	@Override
	protected boolean assemble() {
		List<String> list = Splitter.on("|").splitToList(randomPond);
		for (String rp : list) {
			String[] gw = rp.split("_");
			Gweight wobj = new Gweight();
			wobj.groupId = NumberUtils.toInt(gw[0]);
			wobj.weight = NumberUtils.toInt(gw[1]);
			rpList.add(wobj);
		}
		return super.assemble();
	}

	public int nextGoup() {
		return HawkRand.randomWeightObject(rpList).groupId;
	}

	public int getId() {
		return id;
	}

	public int getUpgradeNum() {
		return upgradeNum;
	}

	public String getRandomPond() {
		return randomPond;
	}

}
