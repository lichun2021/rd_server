package com.hawk.game.battle.effect.impl.hero1107;

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
 * - - 【万分比】【12504】任命在战备参谋部时，集结战斗开始前，我方集结全队出狙击兵数量每达到 X (effect12504NumLimit)万，自身突击步兵防御、生命增加 +XX.XX%(12504)（该效果可叠加，至多 XX (effect12504Maxinum)层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
  - 集结全队出征狙击兵数量每满 X 万
    - 狙击兵（兵种类型 = 6）
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 指定数值读取const表，字段effect12504NumLimit
        - 配置格式：绝对值
  - 自身狙击兵防御、生命 +XX.XX%
    - 自身：这里仅对自己出征的狙击兵生效
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算）
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （该效果可叠加，至多 XX  层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12504Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.DEF, Type.HP })
@EffectChecker(effType = EffType.HERO_12504)
public class Checker12504 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.FOOT_SOLDIER_6 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		int effPer = selectPlayer(parames);

		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		int tankCnt = (int) parames.unitStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_6);

		int cen = Math.min(tankCnt / ConstProperty.getInstance().getEffect12504NumLimit(), ConstProperty.getInstance().getEffect12504Maxinum());
		return cen * parames.unity.getEffVal(effType());
	}

}
