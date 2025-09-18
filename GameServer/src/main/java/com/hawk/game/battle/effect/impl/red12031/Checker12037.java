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
- 【万分比】【12037】战斗开始前，若敌方出征狙击兵（兵种类型 = 6）数量低于我方出征突击步兵（兵种类型 = 5）数量，突击步兵对近战伤害 +XX.XX%
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
- 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
- 此伤害加成与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 +【本作用值】）
- 在战斗开始前判定，满足条件后本次战斗全程生效
- 若敌方出征狙击兵（兵种类型 = 6）数量低于我方出征突击步兵（兵种类型 = 5）数量
  - 数量1 = 敌方出征携带的狙击兵（兵种类型 = 6）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 若为集结，则该数量为敌方所有玩家出征携带的狙击兵数量
  - 数量2 = 己方出征携带的突击步兵（兵种类型 = 5）数量
    - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 若为集结，则该数量为己方所有玩家出征携带的突击步兵数量
  - 数量1/数量2 < 100% 时生效
 */

@BattleTupleType(tuple = Type.HURT)
@EffectChecker(effType = EffType.EFF_12037)
public class Checker12037 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5 && isJinzhan(parames.tarType)) {
			if (parames.tarStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_6) < parames.unitStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_5)) {
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