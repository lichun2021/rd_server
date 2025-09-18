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
 * 【12403】
- 【万分比】【12403】任命在战备参谋部时，防御坦克为其他部队提供护盾后，自身防御坦克生命增加 +XX.XX%（该效果不可叠加，持续 X 回合）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化。
  - （该效果可叠加，至多 X 层 ）
    - 数值读取const表，字段effect12403Maxinum
      - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12403)
public class Checker12403 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

	
}
