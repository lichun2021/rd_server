package com.hawk.game.crossproxy.skill;

/**
 * 持续加buff类型技能
 */
public class CommonCrossSkill extends ICrossSkill {

	private final String skillID;

	public CommonCrossSkill(String skillID) {
		this.skillID = skillID;
	}

	@Override
	public String skillID() {
		return skillID;
	}

}
