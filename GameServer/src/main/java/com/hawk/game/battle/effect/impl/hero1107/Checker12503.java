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
 * - 【万分比】【12503】任命在战备参谋部时，集结战斗开始前，我方集结全队出突击步兵数量每达到 X (effect12503NumLimit)万，自身突击步兵攻击、超能攻击增加 +XX.XX%(12503)（该效果可叠加，至多 XX (effect12503Maxinum)层）
  - 战报相关
    - 于战报中展示
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效且数值不再变化
  - 集结全队出征突击步兵数量每满 X 万
    - 突击步兵（兵种类型 = 5）
    - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
      - 指定数值读取const表，字段effect12503NumLimit
        - 配置格式：绝对值
  - 自身突击步兵攻击，超能攻击 +XX.XX%
    - 自身：这里仅对自己出征的突击步兵生效
    - 该作用号为常规外围属性加成效果，与其他作用号累加计算）
      - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - （该效果可叠加，至多 XX  层）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 层数上限读取const表，字段effect12503Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = { Type.ATK, Type.ATKFIRE })
@EffectChecker(effType = EffType.HERO_12503)
public class Checker12503 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.FOOT_SOLDIER_5 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		// 属性最高的二个人
		int effPer = selectPlayer(parames);

		return new CheckerKVResult(effPer, 0);

	}

	/**数值最高的玩家*/
	private int selectPlayer(CheckerParames parames) {
		int tankCnt = (int) parames.unitStatic.getArmyCountMapMarch().get(SoldierType.FOOT_SOLDIER_5);

		int cen = Math.min(tankCnt / ConstProperty.getInstance().getEffect12503NumLimit(), ConstProperty.getInstance().getEffect12503Maxinum());
		return cen * parames.unity.getEffVal(effType());
	}

}
