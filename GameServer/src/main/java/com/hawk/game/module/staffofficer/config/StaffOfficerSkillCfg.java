package com.hawk.game.module.staffofficer.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.staffofficer.StaffOfficerType;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/staff_officer_skill.xml")
public class StaffOfficerSkillCfg extends HawkConfigBase {
	// <data skillId="10001" skillType="1" skillLevel="1" staffPoint="10000" effectList="100_5000,101_1000" />
	@Id
	protected final int id;
	protected final int skillId;
	protected final int skillType;
	protected final int skillLevel;
	protected final int staffPoint;
	protected final String effectList;

	private StaffOfficerType type;
	private Map<EffType, Integer> effectMap;

	public StaffOfficerSkillCfg() {
		id = 0;
		skillId = 0;
		skillType = 0;
		skillLevel = 0;
		staffPoint = 0;
		effectList = "";
	}

	@Override
	protected boolean assemble() {
		type = StaffOfficerType.valueOf(skillType);
		Map<EffType, Integer> tampMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effectList)) {
			String[] array = effectList.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				tampMap.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}

		this.effectMap = ImmutableMap.copyOf(tampMap);
		return true;
	}

	public int getEffectVal(EffType effType) {
		return effectMap.getOrDefault(effType, 0);
	}

	public Map<EffType, Integer> getEffectMap() {
		return effectMap;
	}

	public void setEffectMap(Map<EffType, Integer> effectMap) {
		this.effectMap = effectMap;
	}

	public int getSkillId() {
		return skillId;
	}

	public int getSkillType() {
		return skillType;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public int getStaffPoint() {
		return staffPoint;
	}

	public String getEffectList() {
		return effectList;
	}

	public StaffOfficerType getType() {
		return type;
	}

	public void setType(StaffOfficerType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

}
