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
//PLANT_SOLDIER_1909 = 1909; //	万分比	泰能主战坦克（兵种类型 = 2，兵种等级 = 13）防御增加	实际防御 = 基础防御*（1 + 其他作用号加成 + 本作用值/10000）
//PLANT_SOLDIER_1910 = 1910; //	万分比	泰能防御坦克（兵种类型 = 1，兵种等级 = 14）防御增加	
//PLANT_SOLDIER_1911 = 1911; //	万分比	泰能直升机（兵种类型 = 4，兵种等级 = 13）防御增加	
//PLANT_SOLDIER_1912 = 1912; //	万分比	泰能轰炸机（兵种类型 = 3，兵种等级 = 14）防御增加	
//PLANT_SOLDIER_1913 = 1913; //	万分比	泰能狙击兵（兵种类型 = 6，兵种等级 = 13）防御增加	
//PLANT_SOLDIER_1914 = 1914; //	万分比	泰能突击步兵（兵种类型 = 5，兵种等级 = 14）防御增加	
//PLANT_SOLDIER_1915 = 1915; //	万分比	泰能采矿车（兵种类型 = 8，兵种等级 = 13）防御增加	
//PLANT_SOLDIER_1916 = 1916; //	万分比	泰能攻城车（兵种类型 = 7，兵种等级 = 14）防御增加	
@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.PLANT_SOLDIER_1913)
public class Checker1913 implements IChecker {
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
