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
 * 【轰炸机】【344】
- 【轰炸机】【泰能钻击爆弹】：轰炸机攻击命中敌方远程部队时，无视其防御加成 +XX.XX%（仅本次攻击生效）
  - 远程部队类型：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
  - 该技能为常规属性外围削减效果，即： 实际属性 = 基础属性*（1 + 其他加成 -【本技能值】）
    - 该防御属性削减效果即对轰炸机攻击时生效
  - 对应技能参数如下
    - trigger：无意义
    - p1：防御属性削减数值
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.CUT_DEF)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_344)
public class CheckerSkill344 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3 && isYuanCheng(parames.tarType)) {
			effPer = parames.solider.getSkill(PBSoldierSkill.PLANE_SOLDIER_3_SKILL_44).getP1IntVal();
		}

		return new CheckerKVResult(effPer, effNum);
	}
}