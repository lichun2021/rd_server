package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 *【12254】
- 【万分比】【12254】突击步兵越过防御坦克的额外攻击，每成功发动 1 次，自身受到伤害减少 +XX.XX%（该效果可叠加，至多 50 层）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此作用号机制是在埃托莉亚的专属作用号【1669】发动的攻击命中敌方时，为突击步兵增加增益效果
    - 须【1669】作用号判定成功且成功命中到敌方目标后才能生效并叠加增益效果（其他攻击方式均无效）
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - （该效果可叠加，至多 50 层）
    - 此效果记录在自身部队身上；可叠加，受叠加次数上限限制
      - 层数上限读取const表，字段effect12254Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12254)
public class Checker12254 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.unity.getEffVal(EffType.HERO_1669) <= 0) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}
}
