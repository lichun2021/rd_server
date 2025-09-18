package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.NATION_HP)
@EffectChecker(effType = EffType.CROSS_EFF_10031)
public class Checker10031 implements IChecker {
	/**
	 * 10029	万分比	主战坦克（兵种类型 = 2）基础生命增加	实际生命 = 基础生命*（1 + 其他外围生命加成）*（1 + 本作用值/10000 + 【10037】/10000 + 【10038】/10000）	这里为基础生命属性，与常规外围生命属性为累乘计算
10030	万分比	防御坦克（兵种类型 = 1）基础生命增加		
10031	万分比	直升机（兵种类型 = 4）基础生命增加		
10032	万分比	轰炸机（兵种类型 = 3）基础生命增加		
10033	万分比	狙击兵（兵种类型 = 6）基础生命增加		
10034	万分比	突击步兵（兵种类型 = 5）基础生命增加		
10035	万分比	采矿车（兵种类型 = 8）基础生命增加		
10036	万分比	攻城车（兵种类型 = 7）基础生命增加		
10037	万分比	于电塔建筑内战斗时，部队（兵种类型 = 1~8）基础生命增加		
10038	万分比	于盟总建筑内战斗时，部队（兵种类型 = 1~8）基础生命增加		
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_4) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
