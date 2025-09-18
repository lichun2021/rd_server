package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.SUPER_SOLDIER_1413)
public class Checker1413 implements IChecker {

	// 轰炸机在发动轰炸时，目标越远，造成伤害越高，对敌人附加（俯冲距离+2）*XX%的伤害。 1413 提升伤害比例=（轰炸机发动30301时与目标距离+2）*作用号/10000，此伤害提升与1075作用号为相加（即在同一个括号内）
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
