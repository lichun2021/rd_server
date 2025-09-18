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
@EffectChecker(effType = EffType.LABRORA_1207)
public class Checker1207 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.MASS.check(parames.troopEffType) && parames.type == SoldierType.CANNON_SOLDIER_8) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}