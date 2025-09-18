package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.WAR_DEF_CITY_FOOT_DEF)
public class Checker195 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (BattleConst.WarEff.CITY_DEF.check(parames.troopEffType) && (parames.type == SoldierType.FOOT_SOLDIER_5 || parames.type == SoldierType.FOOT_SOLDIER_6)) {
			return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
		}
		return new CheckerKVResult(0, 0);
	}
}