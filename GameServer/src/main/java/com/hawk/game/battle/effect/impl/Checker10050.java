package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.BASE_HPD)
public class Checker10050 implements IChecker {
	/**
	 * BASE_DEFD = 10049;// 【10049】部队基础防御减少，万分比。部队基础防御=原基础防御*（1+其他修正-【10049】/10000）
	BASE_HPD  = 10050;// 【10050】部队基础生命减少，万分比。部队基础生命=原基础生命*（1+其他修正-【10050】/10000）
	BASE_ATKD = 10051;// 【10051】部队基础攻击减少，万分比。部队基础攻击=原基础攻击*（1+其他修正-【10051】/10000）
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
