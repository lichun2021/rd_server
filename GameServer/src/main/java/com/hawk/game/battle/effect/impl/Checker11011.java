package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.ARMOUR_11011)
public class Checker11011 implements IChecker {
	/**
	 11011	万分比	采矿车（兵种类型 = 8）在受到近战部队（兵种类型 = 2/1/3/8）攻击时，
	 所受伤害减少XX%	实际受到伤害 = 基础伤害*（1 - 其他作用号减免）*（1 - 本作用值/10000）													
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_8 && isJinzhan(parames.tarType)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
