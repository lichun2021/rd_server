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
 * 【直升机】【444】
- 【直升机】【泰能定点防护】：直升机受到坦克部队攻击时，伤害减少 +XX.XX%
  - 坦克部队类型：主战坦克（兵种类型 = 2）和防御坦克（兵种类型 = 1）
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本技能伤害减少）
  - 对应技能参数如下
    - trigger：无意义
    - p1：伤害减少数值
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_444)
public class CheckerSkill444 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_4 && isTank(parames.tarType)) {
			effPer = parames.solider.getSkill(PBSoldierSkill.PLANE_SOLDIER_4_SKILL_44).getP1IntVal();
		}

		return new CheckerKVResult(effPer, effNum);
	}
}