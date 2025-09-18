package com.hawk.game.battle.effect.impl.eff12111;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * - 【万分比】【12114】壁垒：战斗开始后，己方部队每战损 5%，受到攻击时，伤害减少 +XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号为伤害减少效果，与其他作用号累乘计算
    - 即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 每战损 5%比率数值读取const表，字段effect12114LossThresholdValue
    - 配置格式：万分比
  - 同一玩家不同类型不同等级兵种独立计算战损效果和实际伤害减少数值
  - 该判定在所罗门援军效果之后，即
    - 战斗数量 = 出征数量*（1 + 所罗门援军效果）
    - 战损比率 = 战斗中损失数量/战斗数量
      - 该比率数值取值区间【0,100%】
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12114)
public class Checker12114 implements IChecker {
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
