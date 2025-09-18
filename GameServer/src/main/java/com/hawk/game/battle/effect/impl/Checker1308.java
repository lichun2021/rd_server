package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 【1308】：主战坦克（兵种类型 = 2）占比超过20%时，主战坦克受到主战坦克伤害减少；【万分比】（与其他减少伤害的计算为累乘关系） 注：实际受到伤害 = 基础伤害 * （1 - 某作用号减免） * （1 - 【1308】）
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.SOLDIER_1308)
public class Checker1308 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2 && parames.tarType == SoldierType.TANK_SOLDIER_2) {
			double footCount = parames.getArmyTypeCount(SoldierType.TANK_SOLDIER_2);

			if (footCount / parames.totalCount >= 0.2) {
				effPer = parames.unity.getEffVal(effType());
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
