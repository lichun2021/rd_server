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

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.HERO_1628)
public class Checker1628 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type) && BattleConst.WarEff.CITY_DEF.check(parames.troopEffType)) {
			if (parames.unity.getEffVal(EffType.CITY_ENEMY_MARCH_SPD) > 0) { // 开缓兵之计
				int count = parames.unity.getPlayer().skill10303DurningDead;
				int cen = Math.min(ConstProperty.getInstance().getEffect1628Maxinum(), (count / 10000));
				effPer = parames.unity.getEffVal(effType()) * cen;
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
