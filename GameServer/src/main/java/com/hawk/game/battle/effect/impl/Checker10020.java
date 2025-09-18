package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.NATION_DEF)
@EffectChecker(effType = EffType.CROSS_EFF_10020)
public class Checker10020 implements IChecker {
	/**
	 * 10019	万分比	主战坦克（兵种类型 = 2）基础防御增加	实际防御 = 基础防御*（1 + 其他外围防御加成）*（1 + 本作用值/10000 + 【10027】/10000 + 【10028】/10000）	这里为基础防御属性，与常规外围防御属性为累乘计算
	10020	万分比	防御坦克（兵种类型 = 1）基础防御增加		
	10021	万分比	直升机（兵种类型 = 4）基础防御增加		
	10022	万分比	轰炸机（兵种类型 = 3）基础防御增加		
	10023	万分比	狙击兵（兵种类型 = 6）基础防御增加		
	10024	万分比	突击步兵（兵种类型 = 5）基础防御增加		
	10025	万分比	采矿车（兵种类型 = 8）基础防御增加		
	10026	万分比	攻城车（兵种类型 = 7）基础防御增加		
	10027	万分比	于电塔建筑内战斗时，部队（兵种类型 = 1~8）基础防御增加		
	10028	万分比	于盟总建筑内战斗时，部队（兵种类型 = 1~8）基础防御增加		
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
