package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.CheckerKVResult;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
- 【万分比】【12032】防御坦克（兵种类型 = 1）受到远程部队攻击时，受到伤害减少 +XX.XX%
- 战报相关
  - 于战报中展示
  - 不合并至精简战报中
- 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
- 此伤害减免与其他伤害减免累乘计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成）*（1 - 其他伤害减免）*（1 - 【本作用值】）
 */

@BattleTupleType(tuple = Type.REDUCE_HURT)
@EffectChecker(effType = EffType.EFF_12032)
public class Checker12032 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type == SoldierType.TANK_SOLDIER_1 && isYuanCheng(parames.tarType)) {
			return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
		}
		return CheckerKVResult.DefaultVal;

	}
}