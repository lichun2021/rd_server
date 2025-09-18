package com.hawk.game.battle.effect.impl.skill44;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 【狙击兵】【644】
- 【狙击兵】【泰能潜行伪装】：狙击兵受到敌方步兵攻击时，伤害减少 +XX.XX%
  - 步兵类型：狙击兵（兵种类型 = 6）和突击步兵（兵种类型 = 5）
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本技能伤害减少）
  - 对应技能参数如下
    - trigger：无意义
    - p1：伤害减少数值
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_644)
public class CheckerSkill644 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_6 && isFoot(parames.tarType)) {
			effPer = parames.solider.getSkill(PBSoldierSkill.FOOT_SOLDIER_6_SKILL_44).getP1IntVal();
		}

		return new CheckerKVResult(effPer, effNum);
	}
}