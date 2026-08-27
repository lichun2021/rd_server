package com.hawk.game.battle.effect.impl.hero1122;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hawk.tuple.HawkTuple2;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleTroop;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.sssSolomon.ISSSSolomonPet;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/** Round-aware combat consumption for Lexa's configured effects. */
public final class Hero1122Runtime {
	private static final int VERA_HERO_ID = 1106;
	private static final String SOURCE_CACHE_KEY = Hero1122Runtime.class.getName() + ".sources";
	private static final String AIR_CACHE_KEY = Hero1122Runtime.class.getName() + ".air.";
	private static final String HISTORICAL_AIR_CACHE_KEY = Hero1122Runtime.class.getName() + ".historicalAir.";

	private Hero1122Runtime() {
	}

	public static int attackAdjustment(BattleSoldier soldier) {
		BattleTroop own = soldier.getTroop();
		BattleTroop enemy = own.getEnemyTroop();
		int round = soldier.getBattleRound();
		int ownAir = airValue(own, round);
		int enemyAir = airValue(enemy, round);
		if (ownAir == enemyAir) {
			return 0;
		}
		if (ownAir > enemyAir) {
			return Hero1122Rules.scaledEffect(ownAir - enemyAir, effectSum(own, EffType.HERO_12963), Hero1122Rules.ATTACK_ADJUST_CAP);
		}
		return -Hero1122Rules.scaledEffect(enemyAir - ownAir, effectSum(enemy, EffType.HERO_12963), Hero1122Rules.ATTACK_ADJUST_CAP);
	}

	public static int superAttackBonus(BattleSoldier soldier) {
		return historicalAirMax(soldier.getTroop(), soldier.getBattleRound()) >= Hero1122Rules.AIR_THRESHOLD_SUPER_ATTACK
				? saturatedAdd(effectSum(soldier.getTroop(), EffType.HERO_12964), effectSum(soldier.getTroop(), EffType.HERO_12991)) : 0;
	}

	public static int outgoingDamageBonus(BattleSoldier soldier) {
		return historicalAirMax(soldier.getTroop(), soldier.getBattleRound()) >= Hero1122Rules.AIR_THRESHOLD_ADD_HURT
				? saturatedAdd(effectSum(soldier.getTroop(), EffType.HERO_12965), effectSum(soldier.getTroop(), EffType.HERO_12992)) : 0;
	}

	public static int windFieldExtraDamage(BattleSoldier soldier) {
		BattleTroop troop = soldier.getTroop();
		return hasWindFieldTriggered(troop, soldier.getBattleRound()) ? effectSum(troop, EffType.HERO_12970) : 0;
	}

	public static int incomingDamageReduction(BattleSoldier soldier) {
		BattleTroop own = soldier.getTroop();
		int result = 0;
		int round = soldier.getBattleRound();
		if (soldier.getType() == SoldierType.PLANE_SOLDIER_3 || soldier.getType() == SoldierType.PLANE_SOLDIER_4) {
			int ownAir = airValue(own, round);
			int enemyAir = airValue(own.getEnemyTroop(), round);
			if (ownAir > enemyAir) {
				result += Hero1122Rules.scaledEffect(ownAir - enemyAir, effectSum(own, EffType.HERO_12962), Hero1122Rules.AIR_REDUCE_HURT_CAP);
			}
		}
		if (historicalAirMax(own, round) >= Hero1122Rules.AIR_THRESHOLD_REDUCE_HURT) {
			result += effectSum(own, EffType.HERO_12966);
			result += activeSkillEffectSum(sources(own), EffType.HERO_12993);
		}
		return Math.min(result, 9999);
	}

	public static int bomberInterferenceDodge(BattleSoldier defender, BattleSoldier attacker) {
		if (attacker.getType() != SoldierType.PLANE_SOLDIER_3) {
			return 0;
		}
		int round = defender.getBattleRound();
		List<BattleSoldier> sources = sources(defender.getTroop());
		boolean active = sources.stream().anyMatch(source -> hasVera(source) && Hero1122Rules.isInterferenceRound(round, true));
		if (!active) {
			return 0;
		}
		long bomberUnits = attacker.getTroop().getSoldierList().stream()
				.filter(BattleSoldier::isAlive)
				.filter(unit -> unit.getType() == SoldierType.PLANE_SOLDIER_3)
				.count();
		long value = (long) effectSumWithVera(sources, EffType.HERO_12967) * bomberUnits;
		return (int) Math.min(10000L, Math.max(0L, value));
	}

	static int airValue(BattleTroop troop, int round) {
		Object cached = troop.getExtryParam(AIR_CACHE_KEY + round).orElse(null);
		if (cached instanceof Integer) {
			return (Integer) cached;
		}
		List<BattleSoldier> sources = sources(troop);
		if (sources.isEmpty()) {
			troop.putExtryParam(AIR_CACHE_KEY + round, 0);
			return 0;
		}
		int base = baseAirValue(sources, round);
		int triggerRound = windFieldTriggerRound(sources);
		int result = Hero1122Rules.isWindFieldDouble(round, triggerRound) ? saturatedDouble(base) : base;
		troop.putExtryParam(AIR_CACHE_KEY + round, result);
		return result;
	}

	private static int historicalAirMax(BattleTroop troop, int round) {
		Object cached = troop.getExtryParam(HISTORICAL_AIR_CACHE_KEY + round).orElse(null);
		if (cached instanceof Integer) {
			return (Integer) cached;
		}
		if (round <= 0 || sources(troop).isEmpty()) {
			troop.putExtryParam(HISTORICAL_AIR_CACHE_KEY + round, 0);
			return 0;
		}
		int max = 0;
		int lastChangingRound = Hero1122Rules.WIND_FIELD_FALLBACK_ROUND + Hero1122Rules.WIND_FIELD_DURATION_ROUNDS - 1;
		for (int current = 1; current <= Math.min(round, lastChangingRound); current++) {
			max = Math.max(max, airValue(troop, current));
		}
		if (round > lastChangingRound) {
			max = Math.max(max, airValue(troop, round));
		}
		troop.putExtryParam(HISTORICAL_AIR_CACHE_KEY + round, max);
		return max;
	}

	private static boolean hasWindFieldTriggered(BattleTroop troop, int round) {
		List<BattleSoldier> sources = sources(troop);
		return !sources.isEmpty() && effectSum(sources, EffType.HERO_12970) > 0 && round >= windFieldTriggerRound(sources);
	}

	private static int windFieldTriggerRound(List<BattleSoldier> sources) {
		return Hero1122Rules.windFieldTriggerRound(baseAirValue(sources, 1), baseAirValue(sources, 5), baseAirValue(sources, 10));
	}

	private static int baseAirValue(List<BattleSoldier> sources, int round) {
		long result = 0;
		for (BattleSoldier source : sources) {
			int coefficient = source.getEffVal(EffType.HERO_12961);
			int layers = Hero1122Rules.synergyLayers(round, hasVera(source));
			coefficient = Hero1122Rules.coefficientWithSynergy(coefficient, layers);
			result += Hero1122Rules.airContribution(averageAttributeBonus(source), coefficient);
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}

	private static int averageAttributeBonus(BattleSoldier source) {
		long attack = tupleFirst(source, BattleTupleType.Type.ATK);
		long defence = tupleFirst(source, BattleTupleType.Type.DEF);
		long hp = tupleFirst(source, BattleTupleType.Type.HP);
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (attack + defence + hp) / 3L));
	}

	private static int tupleFirst(BattleSoldier source, BattleTupleType.Type type) {
		HawkTuple2<Integer, Integer> tuple = source.tupleValue(type, SoldierType.XXXXXXXXXXXMAN);
		return tuple == null ? 0 : tuple.first;
	}

	@SuppressWarnings("unchecked")
	private static List<BattleSoldier> sources(BattleTroop troop) {
		Object cached = troop.getExtryParam(SOURCE_CACHE_KEY).orElse(null);
		if (cached instanceof List) {
			return (List<BattleSoldier>) cached;
		}
		List<BattleSoldier> result = new ArrayList<>();
		for (BattleSoldier soldier : troop.getSoldierList()) {
			if (soldier.getType() == SoldierType.PLANE_SOLDIER_4
					&& !(soldier instanceof ISSSSolomonPet)
					&& soldier.getEffVal(EffType.HERO_12961) > 0) {
				result.add(soldier);
			}
		}
		result.sort(Comparator.comparingInt((BattleSoldier source) -> Hero1122Rules.airContribution(
				averageAttributeBonus(source), source.getEffVal(EffType.HERO_12961))).reversed()
				.thenComparing(BattleSoldier::getPlayerId));
		if (result.size() > Hero1122Rules.MAX_EFFECT_PLAYERS) {
			result = new ArrayList<>(result.subList(0, Hero1122Rules.MAX_EFFECT_PLAYERS));
		}
		result = Collections.unmodifiableList(result);
		troop.putExtryParam(SOURCE_CACHE_KEY, result);
		return result;
	}

	private static int effectSum(BattleTroop troop, EffType effect) {
		return effectSum(sources(troop), effect);
	}

	private static int effectSum(List<BattleSoldier> sources, EffType effect) {
		long result = 0;
		for (BattleSoldier source : sources) {
			result += source.getEffVal(effect);
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}

	private static int effectSumWithVera(List<BattleSoldier> sources, EffType effect) {
		long result = 0;
		for (BattleSoldier source : sources) {
			if (hasVera(source)) {
				result += source.getEffVal(effect);
			}
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}

	private static int activeSkillEffectSum(List<BattleSoldier> sources, EffType effect) {
		long result = 0;
		for (BattleSoldier source : sources) {
			if (source.getEffVal(EffType.HERO_12981) > 0) {
				result += source.getEffVal(effect);
			}
		}
		return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
	}

	private static boolean hasVera(BattleSoldier source) {
		return source.getHeros().contains(VERA_HERO_ID);
	}

	private static int saturatedDouble(int value) {
		return value > Integer.MAX_VALUE / 2 ? Integer.MAX_VALUE : value * 2;
	}

	private static int saturatedAdd(int first, int second) {
		long value = (long) first + second;
		return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0L, value);
	}
}
