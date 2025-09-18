package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 12434】
- 【万分比】【12434】战技持续期间，温压爆弹命中步兵单位时，伤害额外 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号仅在尤利娅开启战技后，战技持续期间才生效
  - 此作用号机制是在作用号【12412】命中步兵单位时，该伤害有额外加成
    - 步兵兵种类型包含：狙击兵（兵种类型 = 6）、突击步兵（兵种类型 = 5）
    - 注：仅对【12412】作用号造成的伤害生效
  - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12434)
public class Checker12434 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12421) <= 0) {
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
