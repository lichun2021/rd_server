package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【1647】

在战略技能：资源保护开启的持续期间，守城环境下，判定我方与进攻方之间的士兵数量倍数即为X

生效兵种为全兵种

伤害效果上限 由const.xml effect1647Maxinum 控制 填 10000，即伤害加成最高100%

部队最终伤害 = 基础伤害*（1+其他伤害加成+MIN(【1647】*X,Y）
 */
@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.HERO_1647)
public class Checker1647 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getEffVal(Const.EffType.CITY_RES_PROTECT) > 0 && BattleConst.WarEff.CITY_DEF.check(parames.troopEffType) && isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
			double bei = parames.unitStatic.getTotalCount() / parames.tarStatic.getTotalCount();
			effPer = (int) (effPer * bei);
			effPer = Math.min(effPer, ConstProperty.getInstance().getEffect1647Maxinum());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
