package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import java.util.Objects;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.JIDIEN_1539)
public class Checker1539 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;

		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPer = (int) object;
		} else {
			for (BattleUnity unit : parames.unityList) {
				int val = unit.getEffVal(effType());
				effPer = Math.max(val, effPer);
			}
			parames.putLeaderExtryParam(getSimpleName(), effPer);
		}
		return new CheckerKVResult(effPer, effNum);
	}
}