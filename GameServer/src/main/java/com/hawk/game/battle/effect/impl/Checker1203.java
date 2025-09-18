package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.REDUCE_HURT)
@EffectChecker(effType = EffType.LABRORA_1203)
public class Checker1203 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			if (parames.tarType == SoldierType.PLANE_SOLDIER_4
					|| parames.tarType == SoldierType.FOOT_SOLDIER_5
					|| parames.tarType == SoldierType.FOOT_SOLDIER_6
					|| parames.tarType == SoldierType.CANNON_SOLDIER_7) {
				return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
			}
		}
		return new CheckerKVResult(0, 0);
	}
}