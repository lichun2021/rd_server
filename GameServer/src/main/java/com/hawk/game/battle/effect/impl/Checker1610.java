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
@EffectChecker(effType = EffType.HERO_1610)
public class Checker1610 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		int count = (int) (parames.totalCount - parames.unity.getTarStatic().getTotalCount());
		if (count > 0 && isSoldier(parames.type) && BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			effPer = parames.unity.getEffVal(effType());
			effPer = Math.min(15, count / ConstProperty.getInstance().getEffect1610NumParam()) * effPer;
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
