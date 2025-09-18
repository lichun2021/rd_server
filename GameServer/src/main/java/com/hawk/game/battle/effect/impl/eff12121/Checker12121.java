package com.hawk.game.battle.effect.impl.eff12121;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.EFF_12121)
public class Checker12121 implements IChecker {
	/**
	 【12121】
	- 【万分比】【12121】任命在战备参谋部时，部队伤害增加  +XX.XX%
	- 战报相关
	- 于战报中展示
	- 合并至精简战报中
	- 部队类型包含：1~8兵种类型
	- 该作用号为额外伤害加成，在计算时与其他伤害加成累加计算
	- 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
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
