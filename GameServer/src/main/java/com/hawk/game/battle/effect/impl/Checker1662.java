package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 作用号需求
1662 泰能战士（兵种类型 = 1至8，且士兵等级 = 13至14）的士兵，在阵亡环境下，一定比例转换为伤兵

实际阵亡士兵数量 = 阵亡数量 * 1*（1-其他死转伤比例）*（1 - 作用号1662）

1662 是万分比数
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1662)
public class Checker1662 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.solider.getSoldierCfg().isPlantSoldier()) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
