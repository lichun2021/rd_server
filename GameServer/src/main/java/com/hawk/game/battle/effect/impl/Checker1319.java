package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.JSZ_1319)
public class Checker1319 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse = parames.type == SoldierType.CANNON_SOLDIER_8
				|| parames.type == SoldierType.TANK_SOLDIER_1
				|| parames.type == SoldierType.TANK_SOLDIER_2
				|| parames.type == SoldierType.PLANE_SOLDIER_3;
		if (bfalse) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {

				double per = 0.1 - parames.unity.getEffVal(EffType.HERO_1640) * GsConst.EFF_PER;
				for (String playerId : parames.getAllPlayer()) {
					double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_8);
					double total = parames.getPlayerArmyCount(playerId);
					if (footCount / total >= per) {
						effPer = Math.max(effPer, parames.getPlayerBattleEffVal(playerId, effType()));
					}
				}

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}