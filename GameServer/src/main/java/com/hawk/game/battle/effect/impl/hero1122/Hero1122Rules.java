package com.hawk.game.battle.effect.impl.hero1122;

/** Pure numeric rules for Lexa (local hero 1122). */
public final class Hero1122Rules {
	public static final int MAX_EFFECT_PLAYERS = 2;
	public static final int AIR_REDUCE_HURT_CAP = 1200;
	public static final int ATTACK_ADJUST_CAP = 168000;
	public static final int AIR_THRESHOLD_REDUCE_HURT = 300;
	public static final int AIR_THRESHOLD_ADD_HURT = 500;
	public static final int AIR_THRESHOLD_SUPER_ATTACK = 700;
	public static final int AIR_THRESHOLD_WIND_FIELD = 1000;
	public static final int SYNERGY_INTERVAL_ROUND = 5;
	public static final int SYNERGY_MAX_LAYERS = 2;
	public static final int SYNERGY_COEFFICIENT_ADD = 800;
	public static final int WIND_FIELD_FALLBACK_ROUND = 40;
	public static final int WIND_FIELD_DURATION_ROUNDS = 5;

	private Hero1122Rules() {
	}

	public static boolean qualifies(long helicopterCount, long playerTotal, long rallyTotal) {
		if (helicopterCount <= 0 || playerTotal <= 0 || rallyTotal <= 0) {
			return false;
		}
		return helicopterCount * 2L > playerTotal
				&& helicopterCount * 20L >= rallyTotal;
	}

	public static int airContribution(int averageAttributeBonus, int coefficient) {
		return nonNegativeLongToInt((long) averageAttributeBonus * coefficient / 10000L);
	}

	public static int scaledEffect(int value, int coefficient, int cap) {
		int result = nonNegativeLongToInt((long) Math.max(0, value) * Math.max(0, coefficient) / 10000L);
		return cap > 0 ? Math.min(result, cap) : result;
	}

	public static int synergyLayers(int round, boolean hasVera) {
		if (!hasVera || round < SYNERGY_INTERVAL_ROUND) {
			return 0;
		}
		return Math.min(round / SYNERGY_INTERVAL_ROUND, SYNERGY_MAX_LAYERS);
	}

	public static boolean isInterferenceRound(int round, boolean hasVera) {
		return hasVera && round > 0 && round % SYNERGY_INTERVAL_ROUND == 0;
	}

	/**
	 * The three values are air supremacy before round 5, from round 5, and from
	 * round 10 respectively. The first threshold crossing wins; otherwise round
	 * 40 is the documented fallback.
	 */
	public static int windFieldTriggerRound(int airBeforeFive, int airFromFive, int airFromTen) {
		if (airBeforeFive >= AIR_THRESHOLD_WIND_FIELD) {
			return 1;
		}
		if (airFromFive >= AIR_THRESHOLD_WIND_FIELD) {
			return 5;
		}
		if (airFromTen >= AIR_THRESHOLD_WIND_FIELD) {
			return 10;
		}
		return WIND_FIELD_FALLBACK_ROUND;
	}

	public static boolean isWindFieldDouble(int round, int triggerRound) {
		return round >= triggerRound && round < triggerRound + WIND_FIELD_DURATION_ROUNDS;
	}

	public static int coefficientWithSynergy(int coefficient, int layers) {
		int safeLayers = Math.max(0, Math.min(layers, SYNERGY_MAX_LAYERS));
		return nonNegativeLongToInt((long) coefficient * (10000L + (long) SYNERGY_COEFFICIENT_ADD * safeLayers) / 10000L);
	}

	private static int nonNegativeLongToInt(long value) {
		if (value <= 0) {
			return 0;
		}
		return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
	}
}
