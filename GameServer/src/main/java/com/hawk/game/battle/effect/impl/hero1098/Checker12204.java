package com.hawk.game.battle.effect.impl.hero1098;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12204】磁暴干扰：蓄能炮命中处于【损坏状态】的单位时，伤害额外 +XX.XX%
- 【万分比】【12205】磁暴干扰：蓄能炮命中处于【损坏状态】的单位时，使其【损坏状态】下的减益效果额外 +XX.XX%
- 【万分比】【12206】磁暴干扰：蓄能炮命中坦克单位时，降低其 +XX.XX%的暴击几率（该效果可叠加，至多 30 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 上述3个作用号绑定在作用号【12202】上，仅在【12202】释放范围攻击时生效
  - 该作用号为额外伤害加成，在计算时与其他伤害加成累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
  - 【损坏状态】
    - 【损坏状态】是一种特殊标识，目前唯一来源为杰西卡的引爆技能（即作用号【12163】）
      - 具体为目标单位作用号【12164~12171】任意一个大于0，即为【损坏状态】
  - 使其【损坏状态】下的减益效果额外 +XX.XX%
    - 即目标单位作用号【12164~12171】数值变更为：实际数值 = 当前数值*（1 + 【12205】）
      - 注：这里【12205】重复命中目标时，其数值不会重复叠加，也不会刷新
  - 蓄能炮命中坦克单位时，降低其 +XX.XX%的暴击几率
    - 坦克单位包含有：主战坦克 = 2和防御坦克 = 1
    - 该作用号在计算时与其他作用号累加计算
      - 即 实际暴击率 = 基础暴击率 + 其他作用号加成 - 【本作用值】
        - 注：暴击率为判定类数值，最终取值合法区间为【0,100%】
  - （该效果可叠加，至多 30 层）
    - 该作用号可叠加，受叠加次数上限限制
      - 注：【12201】释放3轮时，每轮攻击都可叠加
      - 层数上限读取const表，字段effect12206Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12204)
public class Checker12204 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12201) == 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = parames.unity.getEffVal(effType());
		int effNum = 0;

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
