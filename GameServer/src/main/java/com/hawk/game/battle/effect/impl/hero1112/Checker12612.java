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
 * 【12612】
- 【万分比】【12612】加深效应：【雷感状态】的单位，受到自身突击步兵的伤害增加+XX.XX%【12612】，如果目标是坦克单位，则效果增加+XX.XX%(effect12612BaseVaule)->增伤针对敌方兵种留个内置系数
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本场战斗全程生效
  - 【雷感状态】的单位，受到自身突击步兵的伤害增加+XX.XX%
    - 伤害增加 即 实际伤害 = 【本作用值】 * 自身兵种修正系数 * 基础伤害 *（1 + 各类加成）
  - 如果目标是坦克单位，则效果增加+XX.XX%
    - 对坦克额外伤害增加 即 对坦克实际伤害 = （【本作用值】 + 对坦克效果增加） * 自身兵种修正系数  * 基础伤害 *（1 + 各类加成）
      - 对坦克效果增加值读取const表，字段effect12612BaseVaule
        - 配置格式：万分比
  - ->增伤针对敌方兵种留个内置系数
    - 实际针对自身各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12612SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
    - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12612)
public class Checker12612 implements IChecker {
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
