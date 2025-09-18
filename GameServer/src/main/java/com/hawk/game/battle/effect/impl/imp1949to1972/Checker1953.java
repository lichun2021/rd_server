package com.hawk.game.battle.effect.impl.imp1949to1972;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
//
//PLANT_SOLDIER_1925 = 1925; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）基础攻击增加	实际基础攻击 = 兵种基础值 + 作用值/10000
//PLANT_SOLDIER_1926 = 1926; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）基础攻击增加	
//PLANT_SOLDIER_1927 = 1927; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）基础攻击增加	
//PLANT_SOLDIER_1928 = 1928; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）基础攻击增加	
//PLANT_SOLDIER_1929 = 1929; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）基础攻击增加	
//PLANT_SOLDIER_1930 = 1930; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）基础攻击增加	
//PLANT_SOLDIER_1931 = 1931; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）基础攻击增加	
//PLANT_SOLDIER_1932 = 1932; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）基础攻击增加	

@BattleTupleType(tuple = Type.ATK_BASE)
@EffectChecker(effType = EffType.SOLDIER_1952)
public class Checker1953 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
