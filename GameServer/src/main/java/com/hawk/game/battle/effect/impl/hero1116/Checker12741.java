package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12741)
public class Checker12741 implements IChecker {
	/**
	 * 【12741】
- 【万分比】【12741】光轨锁定 伤害倍率增加 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号绑定光轨锁定作用号【12722】，作用号生效时，造成伤害增加
  - 此伤害加成为额外伤害加成效果，与【12722】作用号累加计算
    - 即 实际伤害 =基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（【12722作用值】+【本作用值】） *敌方兵种修正系数/10000
    - 各固定值沿用作用号【12722】参数
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12721) == 0) {
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
