package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.SOLIDER_6_1314)
public class Checker1314 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
//		boolean bfalse = false;
//		for (BattleUnity unit : parames.unityList) {
//			if (unit.getArmyInfo().getType() == SoldierType.FOOT_SOLDIER_6) {
//				bfalse = true;
//				break;
//			}
//		}
		boolean bfalse = parames.getArmyTypeCount(SoldierType.FOOT_SOLDIER_6) > 0;
		if (bfalse) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
