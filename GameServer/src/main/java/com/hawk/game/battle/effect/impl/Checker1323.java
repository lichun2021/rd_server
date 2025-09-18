package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.JIJIA_1323)
public class Checker1323 implements IChecker {
	// 【万分比】机甲出征或驻防时，且轰炸机（兵种类型=3）比例超过部队20%时，所有部队生命 +X%   参考作用号1316
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		String playerId = parames.unity.getArmyInfo().getPlayerId();
		double footCount = parames.getPlayerArmyCount(playerId, SoldierType.PLANE_SOLDIER_3);
		if (footCount / parames.getPlayerArmyCount(playerId) >= 0.2) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
