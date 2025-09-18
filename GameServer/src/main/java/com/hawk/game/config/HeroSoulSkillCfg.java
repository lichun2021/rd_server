package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/hero_soul_skill.xml")
public class HeroSoulSkillCfg extends HawkConfigBase {
	// <data id="107801" hero="1078" level="1" unlockStage="1" attr="102_10000" />
	@Id
	protected final int id;// ="14"
	protected final int hero;
	protected final int unlockStage;//
	protected final int level;
	protected final String attr;
	private Map<EffType, Integer> effectList;

	public HeroSoulSkillCfg() {
		this.id = 0;
		this.hero = 0;
		unlockStage = 0;
		attr = "";
		level = 0;
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

	public int getUnlockStage() {
		return unlockStage;
	}

	public String getAttr() {
		return attr;
	}

	public int getLevel() {
		return level;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}

}
