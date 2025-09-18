package com.hawk.game.battle.effect.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.PIFU_1162)
public class Checker1162ATK implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		boolean isMass = BattleConst.WarEff.ATK_MASS.check(parames.troopEffType) || BattleConst.WarEff.DEF_MASS.check(parames.troopEffType);
		if (isMass) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				Set<String> set = new HashSet<>();
				List<Integer> resultList = new ArrayList<>();
				for (BattleUnity unit : parames.unityList) {
					String pid = unit.getPlayer().getId();
					if (set.contains(pid)) {
						continue;
					}
					int val = unit.getEffVal(effType());
					set.add(pid);
					resultList.add(val);
				}
				resultList.sort(Comparator.comparingInt(Integer::intValue).reversed());
				effPer = resultList.stream().limit(10).mapToInt(Integer::intValue).sum();

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}
		}

		return new CheckerKVResult(effPer, effNum);
	}
}