package com.hawk.game.battle.effect.impl.eff12111;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12113】强击：战斗开始后，前 10 回合，己方所有部队伤害 +XX%
  - 战报相关
    - 于战报中隐藏 
    - 不合并至精简战报中
  - 前 10 回合读取const表，字段effect12113ContinueRound
    - 配置格式：第X回合_第Y回合
      - 配置1_10表示第1~10回合全程生效
  - 该作用号为额外伤害加成，在计算时与其他伤害加成累加计算
    - 即 实际伤害 = 基础伤害*（1 + 其他作用号加成 +【本作用值】）
    - 注：这里对轰炸机（兵种类型 = 3）单独处理，仅在其发动兵种技能【俯冲轰炸】【id = 303】时生效
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12113)
public class Checker12113 implements IChecker {
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
