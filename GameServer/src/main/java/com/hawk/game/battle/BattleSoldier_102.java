package com.hawk.game.battle;

import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

public class BattleSoldier_102 extends BattleSoldier {

	@Override
	public SoldierType getType() {
		return SoldierType.WEAPON_ACKACK_102;
	}

	@Override
	public int skillAtkExactly(BattleSoldier defSoldier) {
		SoldierType tarType = defSoldier.getType();
		// <data id="1020101" des="防空火炮（火箭）soldierType=兵种类型" soldierType="3,4"
		// />
		BattleSoldierSkillCfg skill = getSkill(PBSoldierSkill.WEAPON_ACKACK_102_SKILL_1);
		boolean bfasle = tarType == SoldierType.PLANE_SOLDIER_3 || tarType == SoldierType.PLANE_SOLDIER_4;
		if (bfasle && trigerSkill(skill)) {
			return skill.getP1IntVal();
		}

		return super.skillAtkExactly(defSoldier);
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.WEAPON_ACKACK_102_SKILL_66;
	}
	
	@Override
	public int getCostReduce() {
		int costReduce = super.getCostReduce();
		costReduce += getEffVal(EffType.WAR_TRAP_COST_REDUCE);
		return costReduce;
	}
}
