package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

//PLANT_SOLDIER_1941 = 1941; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）基础生命增加	实际基础生命 = 兵种基础值 + 作用值/10000
//PLANT_SOLDIER_1942 = 1942; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）基础生命增加	
//PLANT_SOLDIER_1943 = 1943; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）基础生命增加	
//PLANT_SOLDIER_1944 = 1944; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）基础生命增加	
//PLANT_SOLDIER_1945 = 1945; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）基础生命增加	
//PLANT_SOLDIER_1946 = 1946; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）基础生命增加	
//PLANT_SOLDIER_1947 = 1947; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）基础生命增加	
//PLANT_SOLDIER_1948 = 1948; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）基础PLANT_SOLDIER_命增加	 = 命增加	; //
@BattleTupleType(tuple = Type.HP_BASE)
@EffectChecker(effType = EffType.PLANT_SOLDIER_1945)
public class Checker1945 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6 && parames.solider.getSoldierCfg().isPlantSoldier()) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
