package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.CheckerKVResult;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.SOLDIER_1501)
public class Checker1501 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean isMass = BattleConst.WarEff.ATK_MASS.check(parames.troopEffType) || BattleConst.WarEff.DEF_MASS.check(parames.troopEffType);
		if (isMass && parames.type == SoldierType.PLANE_SOLDIER_3) {

			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {

				Set<String> set = new HashSet<>();
				for (BattleUnity unit : parames.unityList) {
					String pid = unit.getPlayer().getId();
					if (set.contains(pid)) {
						continue;
					}
					set.add(pid);
					int val = unit.getEffVal(effType());
					effPer += val;
				}
				effPer = Math.min(10000, effPer);

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}