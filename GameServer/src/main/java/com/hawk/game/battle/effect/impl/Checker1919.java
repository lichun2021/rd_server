package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
//
//PLANT_SOLDIER_1917 = 1917; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）生命增加	实际生命 = 基础生命*（1 + 其他作用号加成 + 本作用值/10000）
//PLANT_SOLDIER_1918 = 1918; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）生命增加	
//PLANT_SOLDIER_1919 = 1919; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）生命增加	
//PLANT_SOLDIER_1920 = 1920; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）生命增加	
//PLANT_SOLDIER_1921 = 1921; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）生命增加	
//PLANT_SOLDIER_1922 = 1922; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）生命增加	
//PLANT_SOLDIER_1923 = 1923; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）生命增加	
//PLANT_SOLDIER_1924 = 1924; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）生命增加	
@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.PLANT_SOLDIER_1919)
public class Checker1919 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_4 && parames.solider.getSoldierCfg().isPlantSoldier()) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
