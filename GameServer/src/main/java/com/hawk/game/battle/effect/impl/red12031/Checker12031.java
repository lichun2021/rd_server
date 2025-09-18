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
 * - 【万分比】【12031】战斗前25回合，主战坦克（兵种类型 = 2）暴击伤害 +XX.XX%
- 战报相关
  - 于战报中展示
  - 合并至精简战报中
- 此伤害加成与其他伤害加成累加计算；即 实际伤害 = 基础伤害*（1 + 各类伤害加成 +【本作用值】）
- 生效回合读取const表，字段effect12031EffectiveRound
  - 配置格式：开始回合_结束回合
  - 配置1_25表示：第1回合至第25回合，全程生效
 * @author lwt
 * @date 2023年6月25日
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12031)
public class Checker12031 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.TANK_SOLDIER_2) {
			return new CheckerKVResult(0, 0);
		}

		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}