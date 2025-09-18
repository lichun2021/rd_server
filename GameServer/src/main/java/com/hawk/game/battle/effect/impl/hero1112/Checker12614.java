package com.hawk.game.battle.effect.impl.hero1112;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12614】闭环效应：【雷感状态】结束时，受到伤害固定值(effect12614BaseVaule)+XX.XX%【12614】
- 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
- 【雷感状态】结束时
  - 【雷感状态】结束后，回合结束前触发伤害
  - 集结时每个突击步兵单位造成的【雷感状态】，都仅对自身部队生效
- 受到伤害固定值+XX.XX%
  - 伤害率：固定值+XX.XX%
    - 即 实际伤害 = 伤害率 * 基础伤害 *（1 + 各类加成）
  - 固定值读取const表，字段effect12614BaseVaule
    - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12614)
public class Checker12614 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_12611) <= 0) {
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
