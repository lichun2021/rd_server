package com.hawk.game.player.hero.skill;

public class NpcHeroSkill extends IHeroSkill {

	private int skillId;
	private int level;

	@Override
	public int skillID() {
		return skillId;
	}

	@Override
	public int getLevel() {
		return level;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
