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
//PLANT_SOLDIER_1933 = 1933; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）基础防御增加	实际基础防御 = 兵种基础值 + 作用值/10000
//PLANT_SOLDIER_1934 = 1934; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）基础防御增加	
//PLANT_SOLDIER_1935 = 1935; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）基础防御增加	
//PLANT_SOLDIER_1936 = 1936; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）基础防御增加	
//PLANT_SOLDIER_1937 = 1937; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）基础防御增加	
//PLANT_SOLDIER_1938 = 1938; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）基础防御增加	
//PLANT_SOLDIER_1939 = 1939; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）基础防御增加	
//PLANT_SOLDIER_1940 = 1940; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）基础防御增加	
@BattleTupleType(tuple = Type.DEF_BASE)
@EffectChecker(effType = EffType.SOLDIER_1962)
public class Checker1962 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
