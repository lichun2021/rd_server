package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
//【1804】【万分比】攻城车（兵种类型 = 7）受到空军部队（兵种类型 = 4/3）攻击时，伤害减少

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.ARMOUR_1804)
public class Checker1804 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;

		boolean fei = parames.tarType == SoldierType.PLANE_SOLDIER_4
				|| parames.tarType == SoldierType.PLANE_SOLDIER_3;

		if (fei && parames.type == SoldierType.CANNON_SOLDIER_7) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
