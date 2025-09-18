package com.hawk.game.battle.effect.impl.hero1088;

import com.hawk.game.battle.effect.BattleConst.WarEff;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12074)
public class Checker12074 implements IChecker {
	/**
	 *【12074】
	- - 【万分比】【12074】战技持续期间，自身所有直升机受到攻击时，伤害减少 +XX.XX%
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号对玩家携带的所有直升机（兵种类型 = 4）部队均生效
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - 此为英雄战技专属作用号，配置格式如下
    - 作用号id_参数1
      - 参数1：作用值系数
        - 配置格式：浮点数
        - 即本作用值 = 英雄军事值 * 参数1/10000
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.troopEffType != WarEff.NO_EFF && parames.type == SoldierType.PLANE_SOLDIER_4) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
