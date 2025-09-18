package com.hawk.game.battle;

import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

public class BattleSoldier_101 extends BattleSoldier {

	@Override
	public SoldierType getType() {
		return SoldierType.WEAPON_LANDMINE_101;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		SoldierType tarType = defSoldier.getType();
		// <data id="1010101" des="地雷（落石）soldierType=兵种类型" soldierType="1,2" />
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.WEAPON_LANDMINE_101_SKILL_1);
		boolean bfasle = tarType == SoldierType.TANK_SOLDIER_1 || tarType == SoldierType.TANK_SOLDIER_2;
		if (bfasle && trigerSkill(skill)) {
			return skill.getP1IntVal();
		}

		return super.skillAtkExactly(defSoldier);
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.WEAPON_LANDMINE_101_SKILL_66;
	}

	@Override
	public int getCostReduce() {
		int costReduce = super.getCostReduce();
		costReduce += getEffVal(EffType.WAR_TRAP_COST_REDUCE);
		return costReduce;
	}
	
	
}
