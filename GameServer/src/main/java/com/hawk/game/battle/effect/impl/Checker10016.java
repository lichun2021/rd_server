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
@EffectChecker(effType = EffType.CROSS_EFF_10016)
public class Checker10016 implements IChecker {
	/**
	 * 10011	万分比	受到敌方主战坦克（兵种类型 = 2）攻击时，伤害减少	实际伤害 = 基础伤害 *（1 - 其他伤害减免）*（1 - 本作用值/10000）
10012	万分比	受到敌方防御坦克（兵种类型 = 1）攻击时，伤害减少	
10013	万分比	受到敌方直升机（兵种类型 = 4）攻击时，伤害减少	
10014	万分比	受到敌方轰炸机（兵种类型 = 3）攻击时，伤害减少	
10015	万分比	受到敌方狙击兵（兵种类型 = 6）攻击时，伤害减少	
10016	万分比	受到敌方突击步兵（兵种类型 = 5）攻击时，伤害减少	
10017	万分比	受到敌方采矿车（兵种类型 = 8）攻击时，伤害减少	
10018	万分比	受到敌方攻城车（兵种类型 = 7）攻击时，伤害减少	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && parames.tarType == SoldierType.FOOT_SOLDIER_5) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
