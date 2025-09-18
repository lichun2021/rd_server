package com.hawk.game.battle.effect.impl.hero1111;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 *- - 【万分比】【12604】任命在战备参谋部时，集结战斗开始前，集结全队出征防御坦克数量每满 5（effect12604NumLimit） 万，自身防御坦克受到攻击时，伤害减少 +XX.XX%（12604）（该效果可叠加，至多 20(effect12604Maxinum)层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
  - 集结全队出征防御坦克数量每满 5 万
    - 兵种包含有：防御坦克（兵种类型 = 1）
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 指定数值读取const表，字段effect12604NumLimit
        - 配置格式：绝对值
  - 自身防御坦克受到攻击时，伤害减少 +XX.XX%
    - 兵种包含有：防御坦克（兵种类型 = 1）
    - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - （该效果可叠加，至多 20 层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12604Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.REDUCE_HURT_PCT })
@EffectChecker(effType = EffType.HERO_12604)
public class Checker12604 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1) {
			int cen = (int) (parames.unitStatic.getArmyCountMapMarch().get(SoldierType.TANK_SOLDIER_1) / ConstProperty.getInstance().effect12604NumLimit);
			cen = Math.min(cen, ConstProperty.getInstance().effect12604Maxinum);
			effPer = parames.unity.getEffVal(effType()) * cen;
		}
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
