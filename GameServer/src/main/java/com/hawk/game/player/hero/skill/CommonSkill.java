package com.hawk.game.player.hero.skill;

public class CommonSkill extends IHeroSkill {

	private int skillID;

	public CommonSkill(int skillID) {
		this.skillID = skillID;
	}

	@Override
	public int skillID() {
		return skillID;
	}

}
