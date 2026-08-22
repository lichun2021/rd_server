package com.hawk.game.battle.effect.impl.hero1120;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12831)
public class Checker12831 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!check(parames, ConstProperty.getInstance().effect12831SelfNumLimit)) {
			return CheckerKVResult.DefaultVal;
		}
		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	public static boolean check(CheckerParames parames, int selfNumLimit) {
		boolean bomber = parames.type == SoldierType.PLANE_SOLDIER_3;
		if (!bomber) {
			return false;
		}
		BattleUnity maxUnity = parames.getPlayerMaxFreeArmy(
				parames.unity.getPlayerId(), SoldierType.PLANE_SOLDIER_3);
		boolean largestBomber = maxUnity == parames.unity;
		if (!largestBomber) {
			return false;
		}
		String playerId = parames.unity.getPlayerId();
		int bomberCount = parames.unitStatic.getPlayerSoldierCountMarch()
				.get(playerId, SoldierType.PLANE_SOLDIER_3);
		return Hero1120Rules.is12831Eligible(
				parames.unity.getEffVal(EffType.HERO_12831),
				bomber,
				largestBomber,
				bomberCount,
				parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId),
				selfNumLimit);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
