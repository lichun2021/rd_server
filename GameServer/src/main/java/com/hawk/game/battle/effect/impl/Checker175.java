package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.WAR_LANDMINE_ATK_FOOT)
public class Checker175 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		SoldierType type = parames.type;
		SoldierType tarType = parames.tarType;
		if (type == SoldierType.WEAPON_LANDMINE_101 || type == SoldierType.WEAPON_ACKACK_102 || type == SoldierType.WEAPON_ANTI_TANK_103) {
			if (type == SoldierType.WEAPON_LANDMINE_101 && (tarType == SoldierType.FOOT_SOLDIER_5 || tarType == SoldierType.FOOT_SOLDIER_6)) {
				effPer = parames.unity.getEffVal(effType());
			}
		}

		return new CheckerKVResult(effPer, 0);
	}
}
