package com.hawk.game.battle.effect.impl.hero1122;

import java.util.Comparator;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = BattleTupleType.Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.HERO_12981)
public class Checker12981 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.PLANE_SOLDIER_4
				|| parames.unity != maxMarchHelicopter(parames)) {
			return CheckerKVResult.DefaultVal;
		}
		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	private BattleUnity maxMarchHelicopter(CheckerParames parames) {
		String playerId = parames.unity.getPlayerId();
		return parames.unityList.stream()
				.filter(unit -> playerId.equals(unit.getPlayerId()))
				.filter(unit -> unit.getArmyInfo().getType() == SoldierType.PLANE_SOLDIER_4)
				.filter(unit -> unit.getMarchCnt() > 0)
				.max(Comparator.comparingInt(BattleUnity::getMarchCnt)
						.thenComparingInt(BattleUnity::getSoldierLevel)
						.thenComparingInt(unit -> -unit.getBuildingWeight()))
				.orElse(null);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
