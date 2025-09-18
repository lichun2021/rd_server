package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/hero_soul_level.xml")
public class HeroSoulLevelCfg extends HawkConfigBase {
	// id="10780101" hero="1078" stage="1" level="1" consumption="30000_1001078_101" power="1001" attr="100_1001"
	@Id
	protected final int id;// ="14"
	protected final int hero;// ="14"
	protected final int stage;// ="14"
	protected final int level;
	protected final int power;//
	protected final String consumption;//
	protected final String attr;//

	private Map<EffType, Integer> effectList;
	public HeroSoulLevelCfg() {
		this.id = 0;
		this.hero = 0;
		this.stage = 0;
		level = 0;
		power = 0;
		consumption = "";
		attr = "";
	}
	
	@Override
	protected boolean assemble() {
		effectList = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(attr)) {
			String[] array = attr.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(effectList);

		return true;
	}

	public int getId() {
		return id;
	}

	public int getHero() {
		return hero;
	}

	public int getStage() {
		return stage;
	}

	public int getLevel() {
		return level;
	}

	public int getPower() {
		return power;
	}

	public String getConsumption() {
		return consumption;
	}

	public String getAttr() {
		return attr;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}

}
