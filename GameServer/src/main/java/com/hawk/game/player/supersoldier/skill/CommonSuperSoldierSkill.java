package com.hawk.game.player.supersoldier.skill;

public class CommonSuperSoldierSkill extends ISuperSoldierSkill {

	private int skillID;

	public CommonSuperSoldierSkill(int skillID) {
		this.skillID = skillID;
	}

	@Override
	public int skillID() {
		return skillID;
	}

	@Override
	protected void casting() {
	}

	@Override
	public void setSkillID(int skillId) {
		this.skillID = skillId;

	}

}
