package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
// 【1802】【万分比】狙击兵（兵种类型 = 6）攻击远程部队（兵种类型 = 4/6/5/7）时，伤害增加

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.ARMOUR_1802)
public class Checker1802 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;

		boolean yuan = parames.tarType == SoldierType.PLANE_SOLDIER_4
				|| parames.tarType == SoldierType.FOOT_SOLDIER_5
				|| parames.tarType == SoldierType.FOOT_SOLDIER_6
				|| parames.tarType == SoldierType.CANNON_SOLDIER_7;
		
		if (yuan && parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
