package com.hawk.game.battle.effect.impl.hero1120;

import java.util.Arrays;

/** Pure eligibility, round, targeting, charge, stack, and soul rules for hero 1120. */
public final class Hero1120Rules {
	private static final double PERMYRIAD = 0.0001D;

	private Hero1120Rules() {
	}

	public static boolean is12831Eligible(int effectValue, boolean bomber, boolean largestBomber,
			int bomberCount, double playerArmyCount, int selfNumLimit) {
		return effectValue > 0 && bomber && largestBomber
				&& bomberCount / playerArmyCount > selfNumLimit * PERMYRIAD;
	}

	public static boolean isBombingRound(int effectValue, int battleRound, int intervalRound) {
		return effectValue > 0 && battleRound % intervalRound == 0;
	}

	public static int attackTimes(boolean rally, int personalTimes, int rallyTimes) {
		return rally ? rallyTimes : personalTimes;
	}

	public static int[] targetPriority(boolean ranged, int lastTargetType) {
		int[] cycle;
		int index;
		if (ranged) {
			cycle = new int[] {6, 7, 5, 4, 6, 7, 5, 4};
			index = lastTargetType == 6 ? 1 : lastTargetType == 7 ? 2 : lastTargetType == 5 ? 3 : 0;
		} else {
			cycle = new int[] {1, 8, 2, 3, 1, 8, 2, 3};
			index = lastTargetType == 1 ? 1 : lastTargetType == 8 ? 2 : lastTargetType == 2 ? 3 : 0;
		}
		return Arrays.copyOfRange(cycle, index, cycle.length);
	}

	public static int nextCharge(int currentCharge, int increment, int maximum) {
		return Math.min(currentCharge + increment, maximum);
	}

	public static boolean isFullyCharged(int currentCharge, int maximum) {
		return currentCharge == maximum;
	}

	public static boolean isBuffActive(int round, int startRound, int endRound) {
		return round >= startRound && round <= endRound;
	}

	public static int cappedRoundStacks(int battleRound, int intervalRound, int maximum) {
		return Math.min(battleRound / intervalRound, maximum);
	}

	public static int combinedEffectValue(int baseEffectValue, int soulEffectValue) {
		return baseEffectValue + soulEffectValue;
	}

	public static int effectDurationMillis(int baseSeconds, int soulSeconds) {
		return baseSeconds * 1000 + soulSeconds * 1000;
	}
}
