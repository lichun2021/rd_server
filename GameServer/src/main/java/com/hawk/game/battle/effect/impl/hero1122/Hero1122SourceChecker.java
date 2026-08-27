package com.hawk.game.battle.effect.impl.hero1122;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.tuple.HawkTuple2;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.SoldierType;

/** Selects the at-most-two helicopter units that may provide Lexa effects. */
abstract class Hero1122SourceChecker implements IChecker {
	private static final String CACHE_KEY = Hero1122SourceChecker.class.getName() + ".selected";

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)
				|| parames.type != SoldierType.PLANE_SOLDIER_4
				|| !selected(parames).contains(parames.unity)) {
			return CheckerKVResult.DefaultVal;
		}
		return new CheckerKVResult(parames.unity.getEffVal(effType()), 0);
	}

	@SuppressWarnings("unchecked")
	private Set<BattleUnity> selected(CheckerParames parames) {
		Object cached = parames.getLeaderExtryParam(CACHE_KEY);
		if (cached instanceof Set) {
			return (Set<BattleUnity>) cached;
		}

		List<Candidate> candidates = new ArrayList<>();
		for (String playerId : parames.getAllPlayer()) {
			BattleUnity source = maxMarchHelicopter(parames, playerId);
			if (source == null || source.getEffVal(effType12961()) <= 0) {
				continue;
			}
			long helicopterCount = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.PLANE_SOLDIER_4);
			long playerTotal = parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId);
			long rallyTotal = (long) parames.unitStatic.getTotalCountMarch();
			if (!Hero1122Rules.qualifies(helicopterCount, playerTotal, rallyTotal)) {
				continue;
			}
			int contribution = Hero1122Rules.airContribution(averageAttributeBonus(source), source.getEffVal(effType12961()));
			candidates.add(new Candidate(source, contribution));
		}

		candidates.sort(Comparator.comparingInt(Candidate::getContribution).reversed()
				.thenComparing(candidate -> candidate.source.getPlayerId()));
		Set<BattleUnity> result = new HashSet<>();
		for (int i = 0; i < Math.min(Hero1122Rules.MAX_EFFECT_PLAYERS, candidates.size()); i++) {
			result.add(candidates.get(i).source);
		}
		parames.putLeaderExtryParam(CACHE_KEY, result);
		return result;
	}

	private BattleUnity maxMarchHelicopter(CheckerParames parames, String playerId) {
		return parames.unityList.stream()
				.filter(unit -> playerId.equals(unit.getPlayerId()))
				.filter(unit -> unit.getArmyInfo().getType() == SoldierType.PLANE_SOLDIER_4)
				.filter(unit -> unit.getMarchCnt() > 0)
				.max(Comparator.comparingInt(BattleUnity::getMarchCnt)
						.thenComparingInt(BattleUnity::getSoldierLevel)
						.thenComparingInt(unit -> -unit.getBuildingWeight()))
				.orElse(null);
	}

	private int averageAttributeBonus(BattleUnity source) {
		int attack = tupleFirst(source, BattleTupleType.Type.ATK);
		int defence = tupleFirst(source, BattleTupleType.Type.DEF);
		int hp = tupleFirst(source, BattleTupleType.Type.HP);
		return (int) (((long) attack + defence + hp) / 3L);
	}

	private int tupleFirst(BattleUnity source, BattleTupleType.Type type) {
		HawkTuple2<Integer, Integer> tuple = source.getSolider().tupleValue(type, SoldierType.XXXXXXXXXXXMAN);
		return tuple == null ? 0 : tuple.first;
	}

	private com.hawk.game.protocol.Const.EffType effType12961() {
		return com.hawk.game.protocol.Const.EffType.HERO_12961;
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

	private static final class Candidate {
		private final BattleUnity source;
		private final int contribution;

		private Candidate(BattleUnity source, int contribution) {
			this.source = source;
			this.contribution = contribution;
		}

		private int getContribution() {
			return contribution;
		}
	}
}
