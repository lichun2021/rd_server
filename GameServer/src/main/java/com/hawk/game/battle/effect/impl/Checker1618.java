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

@BattleTupleType(tuple = Type.DODGE)
@EffectChecker(effType = EffType.HERO_1618)
public class Checker1618 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse = parames.tarType == SoldierType.PLANE_SOLDIER_3 || parames.tarType == SoldierType.PLANE_SOLDIER_4;
		if (parames.type == SoldierType.FOOT_SOLDIER_6 && bfalse && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
