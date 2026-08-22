package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12721)
public class Checker12721 implements IChecker {
	/**
	 * 涛叔，将新英雄作用号12721描述和效果改为：增加定点猎杀【12361】概率40%（代码写死不配常量了），增加定点猎杀伤害+XX.XX%【12721】；如果12721单上，那就没效果，只有存在【12361】才有效果
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.FOOT_SOLDIER_6)) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
