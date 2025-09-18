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
@EffectChecker(effType = EffType.HERO_1660)
public class Checker1660 implements IChecker {

	/**
	 * 【1660】
	触发逻辑：
	战斗前判断敌人战车的排数（兵种类型 =5&6 ），给我方空军增加 【1660】*排数的空军生命（兵种类型 = 3&4）
	
	最终空军生命加成 = 1+其他百分比生命加成+【1660】*对方战车排数
	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(effType()) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4) {
			long num = parames.tarStatic.getArmyList().stream()
					.filter(u -> u.getType() == SoldierType.CANNON_SOLDIER_7 || u.getType() == SoldierType.CANNON_SOLDIER_8)
					.count();
			effPer = (int) (parames.unity.getEffVal(effType()) * Math.min(num, 1000));
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
