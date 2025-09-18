package com.hawk.game.battle.effect.impl.red12031;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
-- 【万分比】【12038】战斗开始前，若敌方出征狙击兵（兵种类型 = 6）数量不低于我方出征突击步兵（兵种类型 = 5）数量，突击步兵受到狙击兵攻击时，伤害减免 +XX.XX%
- 战报相关
  - 于战报中展示
  - 不合并至精简战报中
- 此伤害减免与其他伤害减免累乘计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成）*（1 - 其他伤害减免）*（1 - 【本作用值】）
- 在战斗开始前判定，满足条件后本次战斗全程生效
- 若敌方出征狙击兵（兵种类型 = 6）数量不低于我方出征突击步兵（兵种类型 = 5）数量
  - 数量1 = 敌方出征携带的狙击兵（兵种类型 = 6）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 若为集结，则该数量为敌方所有玩家出征携带的狙击兵数量
  - 数量2 = 己方出征携带的突击步兵（兵种类型 = 5）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 若为集结，则该数量为己方所有玩家出征携带的突击步兵数量
  - 数量1/数量2 >= 100% 时生效
 */

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12038)
public class Checker12038 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5 && parames.tarType == SoldierType.FOOT_SOLDIER_6) {
			if (parames.tarStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_6) >= parames.unitStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_5)) {
				effPer = parames.unity.getEffVal(effType());
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}

	// @Override
	// public boolean tarTypeSensitive() {
	// return false;
	// }
}