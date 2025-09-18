package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.MASS_1487)
public class Checker1487 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean isMass = BattleConst.WarEff.ATK_MASS.check(parames.troopEffType) || BattleConst.WarEff.DEF_MASS.check(parames.troopEffType);
		if (parames.type == SoldierType.CANNON_SOLDIER_8 && isMass) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}