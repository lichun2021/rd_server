package com.hawk.game.battle;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

public class BattleSoldier_100 extends BattleSoldier {

	@Override
	public SoldierType getType() {
		return SoldierType.BARTIZAN_100;
	}

	@Override
	protected PBSoldierSkill keZhiJiNengId() {
		return PBSoldierSkill.BARTIZAN_100_SKILL_66;
	}

	@Override
	public int getAtkRound() {
		int atkRound = super.getAtkRound();
		int effPer = getEffVal(EffType.WAR_BARTIZAN_SPEED);
		atkRound = (int) Math.ceil(1d * atkRound * GsConst.EFF_RATE / (GsConst.EFF_RATE + effPer));
		return atkRound;
	}
	
}
