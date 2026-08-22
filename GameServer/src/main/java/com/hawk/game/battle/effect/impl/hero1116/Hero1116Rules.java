package com.hawk.game.battle.effect.impl.hero1116;

/** Pure round and duration rules used by hero 1116's runtime effects. */
public final class Hero1116Rules {
	private Hero1116Rules() {
	}

	public static boolean is12721Active(int effectValue) {
		return effectValue > 0;
	}

	public static boolean is12722Round(int effectValue, int battleRound, int intervalRound) {
		return effectValue > 0 && battleRound % intervalRound == 0;
	}

	public static boolean is12729Active(int effectValue, int battleRound, int lastRound) {
		return effectValue > 0 && battleRound <= lastRound;
	}

	public static boolean is12730Active(int effectValue, int battleRound, int firstRound) {
		return effectValue > 0 && battleRound >= firstRound;
	}

	public static boolean isBothSelfFight(boolean selfPersonal, boolean targetPersonal) {
		return selfPersonal && targetPersonal;
	}

	public static int adjustAffectedCount(int freeCount, int effectValue, int soldierAdjust) {
		return (int) (freeCount * 0.0001d * effectValue * 0.0001d * soldierAdjust);
	}

	public static int effectDurationMillis(int baseSeconds, int soulSeconds) {
		return baseSeconds * 1000 + soulSeconds * 1000;
	}
}
