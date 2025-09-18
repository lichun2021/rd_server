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
@EffectChecker(effType = EffType.GANDA_1312)
public class Checker1312 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		String playerId = parames.unity.getArmyInfo().getPlayerId();
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			double footCount = parames.getPlayerArmyCount(playerId, SoldierType.TANK_SOLDIER_2);
			if (footCount / parames.getPlayerArmyCount(playerId) >= 0.2) {
				effPer = parames.unity.getEffVal(effType());
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
