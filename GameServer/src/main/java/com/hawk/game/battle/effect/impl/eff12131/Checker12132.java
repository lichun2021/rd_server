package com.hawk.game.battle.effect.impl.eff12131;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 *- 【万分比】【12132】主战庇护：触发荣耀凯恩的【磁暴聚能】效果后，主战坦克受到坦克攻击时，伤害减少 +XX.XX%（该效果不可叠加，持续 2 回合）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 坦克部队类型包含有：主战坦克（兵种类型 = 2）和防御坦克（兵种类型 = 1）
  - 触发荣耀凯恩的【磁暴聚能】效果后
    - 该作用号绑定英雄荣耀凯恩的专属芯片【磁暴聚能】
    - 荣耀凯恩 = 1078
    - 【磁暴聚能】对应作用号【1654~1657】
[图片]
  - 此伤害减少效果算式与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - 该效果不可叠加，先到先得，持续回合结束后消失
    - 数值不叠加
    - 回合数不可刷新重置
  - 持续回合数读取const表，字段effect12132ContinueRound
 */

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.HERO_12132)
public class Checker12132 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2 && isTank(parames.tarType)) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
