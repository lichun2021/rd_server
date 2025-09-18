package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1530)
public class Checker1530 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_8) {
			String playerId = parames.unity.getArmyInfo().getPlayerId();
			double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_8);
			double per = 0.1 - parames.unity.getEffVal(EffType.HERO_1640) * GsConst.EFF_PER;
			if (footCount / parames.totalCount >= per) {
				BattleUnity maxUnity = parames.getPlayerMaxFreeArmy(playerId, SoldierType.CANNON_SOLDIER_8);
				if (Objects.nonNull(maxUnity) && maxUnity.getArmyId() == parames.unity.getArmyInfo().getArmyId()) {
					effPer = parames.unity.getEffVal(effType());
				}
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
