package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.HURT_PCT)
@EffectChecker(effType = EffType.CROSS_EFF_10009)
public class Checker10009 implements IChecker {
	/**
	 * 10003	万分比	攻击命中敌方主战坦克（兵种类型 = 2）时，伤害增加	实际伤害 = 基础伤害*（1 + 其他伤害加成）*（1 + 本作用值/10000）
	10004	万分比	攻击命中敌方防御坦克（兵种类型 = 1）时，伤害增加	
	10005	万分比	攻击命中敌方直升机（兵种类型 = 4）时，伤害增加	
	10006	万分比	攻击命中敌方轰炸机（兵种类型 = 3）时，伤害增加	
	10007	万分比	攻击命中敌方狙击兵（兵种类型 = 6）时，伤害增加	
	10008	万分比	攻击命中敌方突击步兵（兵种类型 = 5）时，伤害增加	
	10009	万分比	攻击命中敌方采矿车（兵种类型 = 8）时，伤害增加	
	10010	万分比	攻击命中敌方攻城车（兵种类型 = 7）时，伤害增加	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && parames.tarType == SoldierType.CANNON_SOLDIER_8) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
