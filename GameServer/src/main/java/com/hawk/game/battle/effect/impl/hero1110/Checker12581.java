package com.hawk.game.battle.effect.impl.hero1110;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 *【12581】
- 【万分比】【12581】战技持续期间，集结战斗中采矿车受到攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 该作用号为伤害减少效果，与其他作用号累乘计算，即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1_参数2
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
      - 参数2：战技持续时间
        - 配置格式：绝对值（单位：秒）
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12581)
public class Checker12581 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		
		int effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
