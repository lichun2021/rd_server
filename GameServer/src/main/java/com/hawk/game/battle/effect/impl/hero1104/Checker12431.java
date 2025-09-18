package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12431】
- 【万分比】【12431】领域突破触发概率额外 +XX.XX%，且在发动轮番轰炸时，伤害额外 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制为改变【12411】作用号的概率判定数值
    - 即实际概率 = 【12411】/10000 + 【本作用值】
      - 注：此为概率判定数值，最终取值合法区间为【0,100%】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12431)
public class Checker12431 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12411) <= 0) {
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
