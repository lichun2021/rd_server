package com.hawk.game.battle;

import com.hawk.game.config.BattleSoldierSkillCfg;

public class EmptyBattleSoldierSkill extends BattleSoldierSkillCfg {
	private static final EmptyBattleSoldierSkill INSTANCE = new EmptyBattleSoldierSkill();

	private EmptyBattleSoldierSkill() {
	}
	
	@Override
	public int getTrigger() {
		return Integer.MIN_VALUE;
	}

	public static EmptyBattleSoldierSkill getInstance() {
		return INSTANCE;
	}

}
