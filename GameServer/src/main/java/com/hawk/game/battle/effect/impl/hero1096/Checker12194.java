package com.hawk.game.battle.effect.impl.hero1096;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12194】任命在战备参谋部时，轰炸机每受到 1 次攻击后，伤害减少 +XX.XX%（该效果可叠加，至多 20 层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 该作用号对玩家携带的所有轰炸机（兵种类型 = 3）部队均生效
  - 该作用号可叠加，受叠加次数上限限制
    - 层数上限读取const表，字段effect12194Maxinum
      - 配置格式：绝对值
  - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
   */
@BattleTupleType(tuple = { Type.SOLDIER_SKILL })
@EffectChecker(effType = EffType.HERO_12194)
public class Checker12194 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);

	}

}
