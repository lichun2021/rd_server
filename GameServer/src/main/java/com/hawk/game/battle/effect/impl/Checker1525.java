package com.hawk.game.battle.effect.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

//作用号【1525】
//设计目的：输出之前，看要打的这个目标，部队的士兵数字多少，士兵越多，这次造成的伤害越高。
//【每回合需要判定】每回合轮到  阻击兵攻击时(未造成伤害前），读取要攻击目标部队士兵数量，作为参数A，
//A 所在区间 10万/20万/30万/50万/75万
//作用号【1525】的值=参与A所属区间对应的作用值w
//Const表字段 effect1525Power= 0,10000,14000,20000,28000,38000（为万分比数值）
//Eff = 0 ; (A<=100000)
//Eff = 10000;  （100000 < A<= 200000)
//Eff = 14000; （200000 < A<= 300000)
//Eff = 20000; （300000 < A<= 500000)
//Eff = 28000; （500000 < A<= 750000)
//Eff = 38000; （750000 < A )
//狙击兵实际伤害=狙击兵基础伤害*（1+其他伤害加成+Eff*【1525】）
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1525)
public class Checker1525 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	public static int effNum(BattleSoldier attackSoldier, BattleSoldier defSoldier) {
		int attacker1525effVal = attackSoldier.getEffVal(EffType.HERO_1525);
		if (attacker1525effVal == 0) {
			return 0;
		}
		String[] arr = ConstProperty.getInstance().getEffect1525Power().split(",");
		int A = defSoldier.getFreeCnt();
		int e1525 = 0;
		try {
			if (A <= 100000) {
				e1525 = NumberUtils.toInt(arr[0]);
			}
			// Eff = 10000; （100000 < A<= 200000)
			if (100000 < A && A <= 200000) {
				e1525 = NumberUtils.toInt(arr[1]);
			}
			// Eff = 14000; （200000 < A<= 300000)
			if (200000 < A && A <= 300000) {
				e1525 = NumberUtils.toInt(arr[2]);
			}
			// Eff = 20000; （300000 < A<= 500000)
			if (300000 < A && A <= 500000) {
				e1525 = NumberUtils.toInt(arr[3]);
			}
			// Eff = 28000; （500000 < A<= 750000)
			if (500000 < A && A <= 750000) {
				e1525 = NumberUtils.toInt(arr[4]);
			}
			// Eff = 38000; （750000 < A )
			if (750000 < A) {
				e1525 = NumberUtils.toInt(arr[5]);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return (int) Math.ceil(e1525 * 0.0001 * attacker1525effVal);
	}
}
