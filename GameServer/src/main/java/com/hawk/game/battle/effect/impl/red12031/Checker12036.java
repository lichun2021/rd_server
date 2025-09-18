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
- 【万分比】【12036】战斗前25回合，狙击对远程部队伤害 +XX.XX%
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
- 远程部队类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
- 此伤害加成与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 +【本作用值】）
- 生效回合读取const表，字段effect12036EffectiveRound
  - 配置格式：开始回合_结束回合
  - 配置1_25表示：第1回合至第25回合，全程生效
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12036)
public class Checker12036 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6 && isYuanCheng(parames.tarType)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}