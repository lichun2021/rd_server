package com.hawk.robot.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigBase.CombineId;

import com.google.common.base.Splitter;

/**
 * 天赋功能配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/talent_level.xml")
@CombineId(fields = {"talentId", "level"})
public class TalentLevelCfg extends HawkConfigBase {
	protected final int id;

	protected final int level;

	protected final int talentId;

	/** 作用号  格式：作用号id_数值*/
	protected final String effect;

	protected final int skill;

	protected final int point;

	private int effectId;
	private int effectValue;
	
	private static Map<Integer, Integer> talentIdMaxLevel = new HashMap<>();

	public TalentLevelCfg() {
		id = 0;
		level = 0;
		talentId = 0;
		effect = "";
		skill = 0;
		point = 0;
	}

	@Override
	protected boolean assemble() {
		List<String> list = Splitter.on("_").omitEmptyStrings().splitToList(effect);
		if (!list.isEmpty()) {
			effectId = NumberUtils.toInt(list.get(0));
			effectValue = NumberUtils.toInt(list.get(1));
		}
		
		if (!talentIdMaxLevel.containsKey(talentId) || talentIdMaxLevel.get(talentId) < level) {
			talentIdMaxLevel.put(talentId, level);
		} 
		
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getTalentId() {
		return talentId;
	}

	public String getEffect() {
		return effect;
	}

	public int getSkill() {
		return skill;
	}

	public int getPoint() {
		return point;
	}


	public int getEffectId() {
		return effectId;
	}

	public void setEffectId(int effectId) {
		this.effectId = effectId;
	}

	public int getEffectValue() {
		return effectValue;
	}

	public void setEffectValue(int effectValue) {
		this.effectValue = effectValue;
	}
	
	public static int getMaxLevelByTalentId(int talentId) {
		if (!talentIdMaxLevel.containsKey(talentId)) {
			return 0;
		}
		
		return talentIdMaxLevel.get(talentId);
	}
}
