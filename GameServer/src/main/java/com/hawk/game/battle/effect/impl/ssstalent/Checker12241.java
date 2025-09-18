package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12241】
- 【万分比】【12241】在战斗结束时，超时空不计入战损的比率额外 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是将荣耀所罗门的专属作用号【1666】的数值变更
    - 最终数值 = 【1666】*（1 + 【12241】/10000）
      - 最终数值取值区间为【0,100%】

【12242】
- 【万分比】【12242】战技持续期间，幻影部队发起攻击的伤害，额外 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是为荣耀所罗门专属的幻影部队单独增加1个伤害加成作用号
  - 此伤害加成为额外伤害加成效果，与其他作用号累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
 * @author lwt
 * @date 2024年4月30日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12241)
public class Checker12241 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.solider.getShadowCnt() > 0) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
