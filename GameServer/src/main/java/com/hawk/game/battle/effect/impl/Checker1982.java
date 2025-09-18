package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

//FIRE_1980 = 1980;// 部队火焰攻击力加成：1980
//FIRE_1981 = 1981;// 坦克火焰攻击力加成：1981 兵种ID：1、2
//FIRE_1982 = 1982;// 飞机火焰攻击力加成：1982 兵种ID：3、4
//FIRE_1983 = 1983;// 步兵火焰攻击力加成：1983 兵种ID：5、6
//FIRE_1984 = 1984;// 战车火焰攻击力加成：1984 兵种ID：7、8
//FIRE_DEF_1990 = 1990;// 部队火焰抗性：1990
//FIRE_DEF_1991 = 1991;// 坦克火焰抗性加成：1991  兵种ID：1、2
//FIRE_DEF_1992 = 1992;// 飞机火焰抗性加成：1992  兵种ID：3、4
//FIRE_DEF_1993 = 1993;// 步兵火焰抗性加成：1993 兵种ID：5、6
//FIRE_DEF_1994 = 1994;// 战车火焰抗性加成：1994 兵种ID：7、8

@BattleTupleType(tuple = Type.ATKFIRE)
@EffectChecker(effType = EffType.FIRE_1982)
public class Checker1982 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
