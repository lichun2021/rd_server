package com.hawk.game.battle.effect.impl;

import java.util.Comparator;
import java.util.Objects;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.NO_GT60_DEF_PER)
public class Checker1029 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean bfalse;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			bfalse = (boolean) object;
		} else {
			int maxNum = parames.armyList.stream()
					.sorted(Comparator.comparingInt(ArmyInfo::getFreeCnt).reversed())
					.findFirst()
					.get()
					.getFreeCnt();

			bfalse = maxNum / parames.totalCount <= 0.6;
			parames.putLeaderExtryParam(getSimpleName(), bfalse);
		}
		if (bfalse) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
