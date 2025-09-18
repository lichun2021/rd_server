package com.hawk.game.battle.effect.impl;

import java.util.Objects;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;

/**
 * 4050：集结战斗中，每存在一个近战兵种，自身部队防御和生命增加XXX；
4051：集结战斗中，每存在一个远程兵种，自身部队攻击和超能攻击增加XXX
 */
@BattleTupleType(tuple = { Type.DEF, Type.HP })
@EffectChecker(effType = EffType.EFF_4050)
public class Checker4050 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			effPer = (int) object;
		} else {
			int footCount = parames.armyList.stream()
					.filter(ar -> isJinzhan(ar.getType()))
					.mapToInt(ArmyInfo::getFreeCnt)
					.sum();
			effPer = footCount * parames.unity.getEffVal(effType());
			parames.putLeaderExtryParam(getSimpleName(), effPer);
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
