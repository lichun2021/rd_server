package com.hawk.game.battle.effect.impl;

import java.util.Objects;

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
import com.hawk.game.util.GsConst;

/**
 * 作用号逻辑
【1649】
攻城环境下，士兵类型 = 5 和 6 的士兵数量占总体士兵数量的 x% 以上（大于关系）
，x 由const.xml 的 effect1649Per 控制， 填6000，即60%
效果为 ：伤转死比例额外增加
若 泰能步兵数量 即 士兵类型 = 5 和 6 中的battle_soldier level  13 和 14 士兵，占士兵类型 5和6 士兵数量的 y% 以上（大于关系）
y 由const.xml 的 effect1649Per2 控制， 填5000，即50%
伤转死比例 则翻倍
最终伤转死比例 = 其他伤转死比例 + 【1649】
 * @author lwt
 * @date 2022年7月4日
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1649)
public class Checker1649 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (BattleConst.WarEff.CITY_ATK.check(parames.troopEffType) && isSoldier(parames.type)) {

			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {

				double taotal = parames.totalCount;
				double taotal56 = parames.getArmyTypeCount(SoldierType.FOOT_SOLDIER_5) + parames.getArmyTypeCount(SoldierType.FOOT_SOLDIER_6);
				double taotalTai56 = parames.unitStatic.getPlantUnityStatistics().getArmyCountMap().get(SoldierType.FOOT_SOLDIER_5)
						+ parames.unitStatic.getPlantUnityStatistics().getArmyCountMap().get(SoldierType.FOOT_SOLDIER_6);
				if (taotal56 / taotal > ConstProperty.getInstance().getEffect1649Per() * GsConst.EFF_PER) {
					effPer = parames.unity.getEffVal(effType());

					if (taotalTai56 / taotal56 > ConstProperty.getInstance().getEffect1649Per2() * GsConst.EFF_PER) {
						effPer = effPer * 2;
					}
				}

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}

		}
		return new CheckerKVResult(effPer, effNum);
	}
}
