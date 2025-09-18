package com.hawk.game.battle.effect.impl.hero1103;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
/**
 * 【12404】
  - 【万分比】【12404】任命在战备参谋部时，主战坦克暴击后，自身受到伤害减少 +XX.XX%（该效果不可叠加，持续 X 回合）（与其他减伤属性为累乘计算）
    - 战报相关
      - 于战报中展示
      - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
  - 即实际伤害 = 基础伤害*（1 - 其他）
  - （该效果可叠加，至多 X 层 ）
    - 数值读取const表，字段effect12404Maxinum
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12404)
public class Checker12404 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_2) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

	
}
