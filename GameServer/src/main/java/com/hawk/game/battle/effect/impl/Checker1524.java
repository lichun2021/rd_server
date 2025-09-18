package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

//作用号【1524】
//设计目的：输出之前，看对面敌方有多少个目标单位，单位越多，攻击加成越多。
//【每回合需要判定】每回合轮到  阻击兵攻击时(未造成伤害前），读取敌方部队目标数量（按照兵种ID算，集结同ID的话算多个），作为参数A，数量上限，记为参数B（Const表字段，effect1524Maximum），if(参数A>参数B，参数A=参数B，参数A）
//Eff = 【1524】*参数A      
//狙击兵实际攻击=狙击兵基础攻击*（1+其他攻击加成+Eff）
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1524)
public class Checker1524 implements IChecker {
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
		int attacker1524effVal = attackSoldier.getEffVal(EffType.HERO_1524);
		if (attacker1524effVal == 0) {
			return 0;
		}
		long lefcount = defSoldier.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(BattleSoldier::canBeAttack)
				.count();
		lefcount = Math.min(lefcount, ConstProperty.getInstance().getEffect1524Maximum());
		attacker1524effVal = (int) (lefcount * attacker1524effVal);
		return attacker1524effVal;
	}
}
