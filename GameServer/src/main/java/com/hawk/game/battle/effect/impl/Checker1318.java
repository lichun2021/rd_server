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
@EffectChecker(effType = EffType.JSZ_1318)
public class Checker1318 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		String playerId = parames.unity.getArmyInfo().getPlayerId();
		if (parames.type == SoldierType.CANNON_SOLDIER_8) {
			double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_8);
			double total = parames.getPlayerArmyCount(playerId);
			if (footCount / total >= 0.2) {
				effPer = parames.unity.getEffVal(effType());
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}