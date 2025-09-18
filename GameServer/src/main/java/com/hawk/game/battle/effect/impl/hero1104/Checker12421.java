package com.hawk.game.battle.effect.impl.hero1104;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
【12421】
- 【万分比】【12421】战技持续期间，自身出征数量最多的轰炸机受到攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的轰炸机
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的轰炸机（兵种类型 = 3）生效（若存在多个轰炸机数量一样且最高，取等级高的）
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 
    - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即 本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）
 */

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.HERO_12421)
public class Checker12421 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.PLANE_SOLDIER_3)) {
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