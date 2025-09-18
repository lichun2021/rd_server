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
//FIRE_DEF_2011 = 2011;// 防御坦克火焰抗性加成：2011兵种ID：1
//FIRE_DEF_2012 = 2012;// 主站坦克火焰抗性加成：2012兵种ID：2
//FIRE_DEF_2013 = 2013;// 轰炸机火焰抗性加成：2013兵种ID：3
//FIRE_DEF_2014 = 2014;// 直升机火焰抗性加成：2014 兵种ID：4
//FIRE_DEF_2015 = 2015;// 突击步兵火焰抗性加成：2015 兵种ID：5
//FIRE_DEF_2016 = 2016;// 狙击兵火焰抗性加成：2016 兵种ID：6
//FIRE_DEF_2017 = 2017;// 攻城车火焰抗性加成：2017 兵种ID：7
//FIRE_DEF_2018 = 2018;// 采矿车火焰抗性加成：2018 兵种ID：8

@BattleTupleType(tuple = Type.DEFFIRE)
@EffectChecker(effType = EffType.FIRE_DEF_2016)
public class Checker2016 implements IChecker {
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
