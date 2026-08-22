package com.hawk.game.battle.effect.impl.hero1118;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleUnity;
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

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12781)
public class Checker12781 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.TANK_SOLDIER_1 || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_1)) {
			return CheckerKVResult.DefaultVal;
		}

		@SuppressWarnings("unchecked")
		Map<String, Integer> effPlayerVal = (Map<String, Integer>) parames.getLeaderExtryParam(getSimpleName());
		if (effPlayerVal == null) {
			effPlayerVal = selectPlayer(parames);
			parames.putLeaderExtryParam(getSimpleName(), effPlayerVal);
		}
		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}
		return new CheckerKVResult(effPlayerVal.get(parames.unity.getPlayerId()), 0);
	}

	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new LinkedHashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			valMap.put(unity.getPlayerId(), effvalue(unity, parames));
		}
		int maximum = Hero1118Rules.cappedStackCount(valMap.size(), ConstProperty.getInstance().effect12781Maxinum);
		return valMap.entrySet().stream()
				.sorted((item1, item2) -> item2.getValue().compareTo(item1.getValue()))
				.filter(entry -> entry.getValue() > 0)
				.limit(maximum)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			String playerId = unity.getPlayerId();
			int defenseTankCount = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.TANK_SOLDIER_1);
			if (Hero1118Rules.is12781Eligible(
					unity.getEffVal(effType()),
					defenseTankCount,
					parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId),
					parames.unitStatic.getTotalCountMarch(),
					ConstProperty.getInstance().effect12781SelfNumLimit,
					ConstProperty.getInstance().effect12781AllNumLimit)) {
				return unity.getEffVal(effType());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
