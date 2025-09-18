package com.hawk.game.battle.effect.impl.hero1106;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *  - 【万分比】【12481~12482】触发空中缠斗时，有 【固定值(effect12481BaseVaule) + XX.XX%【12481】*敌方空军单位数】 的概率将目标敌方数由 1 个增至 2 个，且自身受到缠斗目标伤害时，额外减少 +XX.XX%【12482】
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 此作用号绑定空中缠斗作用号【12461】，作用号生效时，额外触发以下效果
  - 有 【固定值 + XX.XX%*敌方空军单位数】 的概率将目标敌方数由 1 个增至 2 个
    - 该作用号固定数值读取const表，字段effect12481BaseVaule
      - 配置格式：万分比
    - 此作用号机制为改变【12461】作用号生效时可选取的目标的数量
      - 特殊的：此处概率性数值可支持高于100%
        - 以210%为例：则表示必定 +2个目标，且有10%概率额外再 +1个目标
      - 每次发起攻击时独立判定
  - 自身受到缠斗目标伤害时，额外减少 +XX.XX%
    - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
      - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12482)
public class Checker12482 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12461) <= 0) {
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
