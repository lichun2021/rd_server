package com.hawk.game.battle;

import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

public class BattleSoldier_103 extends BattleSoldier {

	@Override
	public SoldierType getType() {
		return SoldierType.WEAPON_ANTI_TANK_103;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		SoldierType tarType = defSoldier.getType();
		// <data id="1030101" des="反坦克火箭（滚木）soldierType=兵种类型" soldierType="5,6"
		// />
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.WEAPON_ANTI_TANK_103_SKILL_1);
		boolean bfasle = tarType == SoldierType.FOOT_SOLDIER_5 || tarType == SoldierType.FOOT_SOLDIER_6;
		if (bfasle && trigerSkill(skill)) {
			return skill.getP1IntVal();
		}

		return super.skillAtkExactly(defSoldier);
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.WEAPON_ANTI_TANK_103_SKILL_66;
	}
	
	@Override
	public int getCostReduce() {
		int costReduce = super.getCostReduce();
		costReduce += getEffVal(EffType.WAR_TRAP_COST_REDUCE);
		return costReduce;
	}
}
