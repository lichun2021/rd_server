package com.hawk.game.battle.effect.impl.hero1109;

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
 * 12555】
- 【万分比】【12555】任命在战备参谋部时，集结战斗开始前，集结全队出征攻城车数量每满 5（effect12555NumLimit） 万，自身攻城车受到攻击时，伤害减少 +XX.XX%（12555）（该效果可叠加，至多 20(effect12555Maxinum)层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
  - 集结全队出征攻城车数量每满 5 万
    - 兵种包含有：攻城车（兵种类型 = 7）
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 指定数值读取const表，字段effect12555NumLimit
        - 配置格式：绝对值
  - 自身攻城车受到攻击时，伤害减少 +XX.XX%
    - 兵种包含有：攻城车（兵种类型 = 7）
    - 该伤害减少效果与其他伤害减少效果累乘计算；即实际伤害 = 基础伤害*（1 + 敌方各类伤害加成）*（1 - 己方某伤害减少）*（1 - 本作用值伤害减少）
  - （该效果可叠加，至多 20 层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12555Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.REDUCE_HURT_PCT })
@EffectChecker(effType = EffType.EFF_12555)
public class Checker12555 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.CANNON_SOLDIER_7 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		int effPer = selectPlayer(parames);

		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		int tankCnt = (int) parames.unitStatic.getArmyCountMapMarch().get(SoldierType.CANNON_SOLDIER_7);

		int cen = Math.min(tankCnt / ConstProperty.getInstance().effect12555NumLimit, ConstProperty.getInstance().effect12555Maxinum);
		return cen * parames.unity.getEffVal(effType());
	}

}
