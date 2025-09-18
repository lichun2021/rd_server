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
@EffectChecker(effType = EffType.WAR_LESS_PLANE_GET_HURT)
public class Checker128 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4){
			return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
		}
		return new CheckerKVResult(0, 0);
	}
}