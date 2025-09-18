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
@EffectChecker(effType = EffType.AILISHA_1510)
public class Checker1510 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		SoldierType type = parames.type;
		SoldierType tarType = parames.tarType;
		if (type == SoldierType.CANNON_SOLDIER_7 && tarType == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, 0);
	}
}
