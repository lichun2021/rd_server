package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 
【12273】
- 【万分比】【12273】战技持续期间，自身所有空军单位受到攻击时，伤害额外减少 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 注：此作用号须在伊娜莉莎开启战技，战技持续期间才生效
  - 该作用号对玩家携带的所有空军部队均生效
    - 空军部队类型包含有：直升机（兵种类型 = 4）和轰炸机（兵种类型 = 3）
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12273)
public class Checker12273 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.EFF_12074) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
