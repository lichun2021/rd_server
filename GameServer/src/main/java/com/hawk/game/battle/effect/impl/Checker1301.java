package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.SUPER_SOLDIER_1301)
public class Checker1301 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1 || parames.type == SoldierType.TANK_SOLDIER_2) {
			String playerId = parames.unity.getPlayer().getId();
//			double totalCount = 0;
//			int footCount = 0;
//			for (BattleUnity unit : parames.unityList) {
//				if (!Objects.equals(unit.getPlayer().getId(), playerId)) {
//					continue;
//				}
//				totalCount += unit.getArmyInfo().getFreeCnt();
//				if (unit.getArmyInfo().getType() == SoldierType.TANK_SOLDIER_1) {
//					footCount += unit.getArmyInfo().getFreeCnt();
//				}
//			}

			double totalCount = parames.getPlayerArmyCount(playerId);
			double footCount = parames.getPlayerArmyCount(playerId, SoldierType.TANK_SOLDIER_1);

			if (footCount / totalCount >= ConstProperty.getInstance().getGouda_percent()) {
				effPer = parames.unity.getEffVal(effType());
			}

		}

		return new CheckerKVResult(effPer, effNum);
	}
}
