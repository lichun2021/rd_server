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
 * 【12402】
  - 【万分比】【12402】任命在战备参谋部时，主战坦克每受到远程单位的 1 次伤害时，自身主战攻击，超能攻击增加+XX.XX%（该效果可叠加，至多 X 层）
    - 战报相关
      - 于战报中展示
      - 合并至精简战报中
    - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化。
    - 主战坦克每受到远程单位的 1 次伤害时
      - 远程部队类型包含有：直升机 = 4、狙击兵 = 6、突击步兵 = 5和攻城车 = 7
    - 自身主战坦克防御，生命 +XX.XX%
      - 防御坦克 = 2
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
    - （该效果可叠加，至多 X 层）
      - 数值读取const表，字段effect12402Maxinum
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12402)
public class Checker12402 implements IChecker {

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
