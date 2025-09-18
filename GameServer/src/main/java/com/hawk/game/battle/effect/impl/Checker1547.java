package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.HERO_1547)
public class Checker1547 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;

		int effVal = parames.unity.getEffVal(effType());
		if (isSoldier(parames.type) && effVal > 0 && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			int effect1547Maxinum = ConstProperty.getInstance().getEffect1547Maxinum();
			double selfCnt = 200_000;
			int ceng = (int) (parames.totalCount / selfCnt);
			effPer = effVal * Math.min(ceng, effect1547Maxinum);
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
