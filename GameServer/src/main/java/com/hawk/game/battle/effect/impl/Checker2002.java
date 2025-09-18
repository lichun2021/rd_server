package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

//FIRE_2001 = 2001;// 防御坦克火焰攻击力加成：2001兵种ID：1
//FIRE_2002 = 2002;// 主站坦克火焰攻击力加成：2002 兵种ID：2
//FIRE_2003 = 2003;// 轰炸机火焰攻击力加成：2003兵种ID：3
//FIRE_2004 = 2004;// 直升机火焰攻击力加成：2004兵种ID：4
//FIRE_2005 = 2005;// 突击步兵火焰攻击力加成：2005 兵种ID：5
//FIRE_2006 = 2006;// 狙击兵火焰攻击力加成：2006 兵种ID：6
//FIRE_2007 = 2007;// 攻城车火焰攻击力加成：2007 兵种ID：7
//FIRE_2008 = 2008;// 采矿车火焰攻击力加成：2008 兵种ID：8

@BattleTupleType(tuple = Type.ATKFIRE)
@EffectChecker(effType = EffType.FIRE_2002)
public class Checker2002 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
