package com.hawk.game.battle.effect.impl.hero1099;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12224)
public class Checker12224 implements IChecker {
	/**
	 *- 【万分比】【12224】任命在战备参谋部时，己方任意单位每受到 1 次攻击后，自身所有单位受到伤害减少 +XX.XX%（该效果可叠加，至多 100 层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗中随时变化，达到层数上限后不再变化
  - 己方任意单位每受到 1 次攻击后
    - 己方：若有盟友部队，盟友部队被攻击也算在内
    - 任意单位类型包含有：兵种（1~8）
    - 若为范围攻击，则按攻击命中的己方单位实际个数进行计算
    - 敌方单位包含有：兵种（1~8）；光棱塔、城防武器这些不算在内
  - 自身所有单位受到伤害减少 +XX.XX%
    - 所有单位包含有：兵种（1~8）
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - （该效果可叠加，至多 100 层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12224Maxinum
        - 配置格式：绝对值
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

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
