package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import java.util.Objects;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.GANDA_1310)
public class Checker1310 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			bfalse = (boolean) object;
		} else {
			int footCount = parames.armyList.stream()
					.filter(ar -> ar.getType() == SoldierType.CANNON_SOLDIER_7)
					.mapToInt(ArmyInfo::getFreeCnt)
					.sum();

			bfalse = footCount / parames.totalCount >= 0.2;

			parames.putLeaderExtryParam(getSimpleName(), bfalse);
		}
		if (bfalse) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
