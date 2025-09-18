package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEAD_TO_WOUND)
@EffectChecker(effType = EffType.DEAD_TO_WOUND_TANK_A)
public class Checker1061 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, 0);
	}
}
