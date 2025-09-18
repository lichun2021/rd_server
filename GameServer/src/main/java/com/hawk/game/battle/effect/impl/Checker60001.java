package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.ADD_ARMY_ATK)
public class Checker60001 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.SELF_ATK.check(parames.troopEffType)
				&& parames.unity.getEffVal(effType()) > 0) {
			double totalCount = parames.totalCount;
			if (totalCount >= 100_000) {
				effPer = 500;
			}
			if (totalCount >= 200_000) {
				effPer = 1000;
			}
			if (totalCount >= 300_000) {
				effPer = 1500;
			}
			if (totalCount >= 500_000) {
				effPer = 2000;
			}

		}
		return new CheckerKVResult(effPer, effNum);
	}
}
