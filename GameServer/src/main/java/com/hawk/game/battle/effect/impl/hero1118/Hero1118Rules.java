package com.hawk.game.battle.effect.impl.hero1118;

/** Pure eligibility, stance, stacking, and soul rules for hero 1118. */
public final class Hero1118Rules {
	private static final double PERMYRIAD = 0.0001D;

	private Hero1118Rules() {
	}

	public static boolean is12781Eligible(int effectValue, int defenseTankCount, double playerArmyCount,
			double totalArmyCount, int selfNumLimit, int allNumLimit) {
		return effectValue > 0
				&& defenseTankCount / totalArmyCount >= allNumLimit * PERMYRIAD
				&& defenseTankCount / playerArmyCount > selfNumLimit * PERMYRIAD;
	}

	public static boolean shouldSwitchStance(int effectValue, int battleRound, int intervalRound) {
		return effectValue > 0 && battleRound % intervalRound == 0;
	}

	public static int nextStance(int currentStance) {
		return currentStance == 1 ? 2 : 1;
	}

	public static int cappedStackCount(int candidateCount, int maximum) {
		return Math.min(candidateCount, maximum);
	}

	public static boolean isLinkedSoulEffectActive(int requiredEffectValue) {
		return requiredEffectValue > 0;
	}

	public static int combinedEffectValue(int baseEffectValue, int soulEffectValue) {
		return baseEffectValue + soulEffectValue;
	}

	public static int combineAdditiveDamageBonus(int currentBonus, int addedBonus) {
		return currentBonus + addedBonus;
	}

	public static int effectDurationMillis(int baseSeconds, int soulSeconds) {
		return baseSeconds * 1000 + soulSeconds * 1000;
	}
}
