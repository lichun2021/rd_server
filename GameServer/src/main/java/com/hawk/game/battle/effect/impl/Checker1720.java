package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
//TBLY_EFF_1719 = 1719;// 	在泰伯矿点战斗时，部队基础攻击增加	1719
//TBLY_EFF_1720 = 1720;// 在泰伯矿点战斗时，部队基础防御增加	1720
//TBLY_EFF_1721 = 1721;// 在泰伯矿点战斗时，部队基础生命增加	1721	

@BattleTupleType(tuple = Type.DEF_BASE)
@EffectChecker(effType = EffType.TBLY_EFF_1720)
public class Checker1720 implements IChecker {
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
