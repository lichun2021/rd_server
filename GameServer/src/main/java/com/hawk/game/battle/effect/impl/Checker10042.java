package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.NATION_ATK)
@EffectChecker(effType = EffType.CROSS_EFF_10042)
public class Checker10042 implements IChecker {
	/**
	 * 10039	万分比	主战坦克（兵种类型 = 2）基础攻击增加	实际攻击 = 基础攻击*（1 + 其他外围攻击加成）*（1 + 本作用值/10000 + 【10047】/10000 + 【10048】/10000）
	10040	万分比	防御坦克（兵种类型 = 1）基础攻击增加	
	10041	万分比	直升机（兵种类型 = 4）基础攻击增加	
	10042	万分比	轰炸机（兵种类型 = 3）基础攻击增加	
	10043	万分比	狙击兵（兵种类型 = 6）基础攻击增加	
	10044	万分比	突击步兵（兵种类型 = 5）基础攻击增加	
	10045	万分比	采矿车（兵种类型 = 8）基础攻击增加	
	10046	万分比	攻城车（兵种类型 = 7）基础攻击增加	
	10047	万分比	于电塔建筑内战斗时，部队（兵种类型 = 1~8）基础攻击增加	
	10048	万分比	于盟总建筑内战斗时，部队（兵种类型 = 1~8）基础攻击增加	
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
