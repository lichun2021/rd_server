package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
//【【1803】【万分比】采矿车（兵种类型 = 8）受到近战部队（兵种类型 = 2/1/3/8）攻击时，伤害减少

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.ARMOUR_1803)
public class Checker1803 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		
		boolean jin = parames.tarType == SoldierType.TANK_SOLDIER_1
				|| parames.tarType == SoldierType.TANK_SOLDIER_2
				|| parames.tarType == SoldierType.PLANE_SOLDIER_3
				|| parames.tarType == SoldierType.CANNON_SOLDIER_8;
		
		if (jin && parames.type == SoldierType.CANNON_SOLDIER_8) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
