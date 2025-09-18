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

@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.SUPER_SOLDIER_1416)
public class Checker1416 implements IChecker {

	// 空军部队占出征部队20%以上时，全部队提升X%生命值加成。 1416 20%为固定常量（读const表中，gouda_percent字段），X%=作用号/10000，当兵种类型type=3与type=4的部队之和占单人出征的30%触发，仅个人单独计算，不会计算全部队
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		String playerId = parames.unity.getPlayer().getId();
		double totalCount = parames.getPlayerArmyCount(playerId);
		double footCount = parames.getPlayerArmyCount(playerId, SoldierType.PLANE_SOLDIER_3) + parames.getPlayerArmyCount(playerId, SoldierType.PLANE_SOLDIER_4);

		int effPer = 0;
		int effNum = 0;
		if (footCount / totalCount >= ConstProperty.getInstance().getGouda_percent()) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
