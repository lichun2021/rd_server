package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.WAR_CANNON_DEF_NUM)
public class Checker121 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type == SoldierType.CANNON_SOLDIER_7 || parames.type == SoldierType.CANNON_SOLDIER_8) {
			return new CheckerKVResult(0, parames.unity.getEffVal(effType()));
		}
		return new CheckerKVResult(0, 0);
	}
}