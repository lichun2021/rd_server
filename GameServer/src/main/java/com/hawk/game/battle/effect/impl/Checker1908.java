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
//PLANT_SOLDIER_1901 = 1901; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）攻击增加	实际攻击 = 基础攻击*（1 + 其他作用号加成 + 本作用值/10000）
//PLANT_SOLDIER_1902 = 1902; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）攻击增加	
//PLANT_SOLDIER_1903 = 1903; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）攻击增加	
//PLANT_SOLDIER_1904 = 1904; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）攻击增加	
//PLANT_SOLDIER_1905 = 1905; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）攻击增加	
//PLANT_SOLDIER_1906 = 1906; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）攻击增加	
//PLANT_SOLDIER_1907 = 1907; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）攻击增加	
//PLANT_SOLDIER_1908 = 1908; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）攻击增加	

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.PLANT_SOLDIER_1908)
public class Checker1908 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_7 && parames.solider.getSoldierCfg().isPlantSoldier()) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
