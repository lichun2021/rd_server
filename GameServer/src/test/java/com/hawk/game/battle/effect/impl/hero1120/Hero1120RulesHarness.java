package com.hawk.game.battle.effect.impl.hero1120;

import java.util.Arrays;

public final class Hero1120RulesHarness {
	private Hero1120RulesHarness() {
	}

	public static void main(String[] args) {
		assertFalse(Hero1120Rules.is12831Eligible(0, true, true, 501, 1000, 5000),
				"12831 requires a positive root effect");
		assertFalse(Hero1120Rules.is12831Eligible(1, false, true, 501, 1000, 5000),
				"12831 requires a bomber unit");
		assertFalse(Hero1120Rules.is12831Eligible(1, true, false, 501, 1000, 5000),
				"12831 requires the largest deployed bomber unit");
		assertFalse(Hero1120Rules.is12831Eligible(1, true, true, 500, 1000, 5000),
				"12831 bomber share is strict at its threshold");
		assertTrue(Hero1120Rules.is12831Eligible(1, true, true, 501, 1000, 5000),
				"12831 activates above its bomber-share threshold");

		assertFalse(Hero1120Rules.isBombingRound(0, 5, 5), "bombing requires 12831");
		assertFalse(Hero1120Rules.isBombingRound(1, 4, 5), "bombing skips rounds before the interval");
		assertTrue(Hero1120Rules.isBombingRound(1, 5, 5), "bombing includes the interval boundary");
		assertTrue(Hero1120Rules.isBombingRound(1, 10, 5), "bombing repeats on interval boundaries");

		assertEquals(9, Hero1120Rules.attackTimes(false, 9, 18), "personal combat uses the personal count");
		assertEquals(18, Hero1120Rules.attackTimes(true, 9, 18), "rally combat uses the rally count");
		assertArrayEquals(new int[] {6, 7, 5, 4, 6, 7, 5, 4},
				Hero1120Rules.targetPriority(true, 4), "ranged targeting begins with snipers");
		assertArrayEquals(new int[] {7, 5, 4, 6, 7, 5, 4},
				Hero1120Rules.targetPriority(true, 6), "ranged targeting advances after the last type");
		assertArrayEquals(new int[] {1, 8, 2, 3, 1, 8, 2, 3},
				Hero1120Rules.targetPriority(false, 3), "melee targeting begins with defensive tanks");
		assertArrayEquals(new int[] {2, 3, 1, 8, 2, 3},
				Hero1120Rules.targetPriority(false, 8), "melee targeting advances after the last type");

		assertEquals(99, Hero1120Rules.nextCharge(98, 1, 100), "charge increments below its cap");
		assertEquals(100, Hero1120Rules.nextCharge(99, 2, 100), "charge includes and stops at its cap");
		assertFalse(Hero1120Rules.isFullyCharged(99, 100), "wing buff waits below full charge");
		assertTrue(Hero1120Rules.isFullyCharged(100, 100), "wing buff triggers at full charge");
		assertTrue(Hero1120Rules.isBuffActive(10, 10, 15), "wing buff includes its start round");
		assertTrue(Hero1120Rules.isBuffActive(15, 10, 15), "wing buff includes its end round");
		assertFalse(Hero1120Rules.isBuffActive(16, 10, 15), "wing buff expires after its end round");

		assertEquals(0, Hero1120Rules.cappedRoundStacks(4, 5, 2), "12838 has no stack before its interval");
		assertEquals(1, Hero1120Rules.cappedRoundStacks(5, 5, 2), "12838 includes its first interval");
		assertEquals(2, Hero1120Rules.cappedRoundStacks(15, 5, 2), "12838 stops at its configured cap");

		assertEquals(3500, Hero1120Rules.combinedEffectValue(2000, 1500),
				"12851/12852/12853 add to their linked base effects");
		assertEquals(300000, Hero1120Rules.effectDurationMillis(300, 0), "base duration is seconds");
		assertEquals(315000, Hero1120Rules.effectDurationMillis(300, 15), "12854 extends duration in seconds");
	}

	private static void assertTrue(boolean value, String message) {
		if (!value) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean value, String message) {
		assertTrue(!value, message);
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + ": expected " + expected + ", got " + actual);
		}
	}

	private static void assertArrayEquals(int[] expected, int[] actual, String message) {
		if (!Arrays.equals(expected, actual)) {
			throw new AssertionError(message + ": expected " + Arrays.toString(expected)
					+ ", got " + Arrays.toString(actual));
		}
	}
}
