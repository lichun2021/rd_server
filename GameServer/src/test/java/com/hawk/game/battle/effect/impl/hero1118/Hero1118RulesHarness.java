package com.hawk.game.battle.effect.impl.hero1118;

public final class Hero1118RulesHarness {
	private Hero1118RulesHarness() {
	}

	public static void main(String[] args) {
		assertFalse(
				Hero1118Rules.is12781Eligible(0, 501, 1000, 10000, 5000, 500),
				"12781 requires a positive root effect");
		assertFalse(
				Hero1118Rules.is12781Eligible(1, 49, 90, 1000, 5000, 500),
				"12781 rejects a share below the rally threshold");
		assertFalse(
				Hero1118Rules.is12781Eligible(1, 500, 1000, 10000, 5000, 500),
				"12781 self share is strict at its boundary");
		assertTrue(
				Hero1118Rules.is12781Eligible(1, 501, 1000, 10000, 5000, 500),
				"12781 includes the rally boundary and accepts an above-boundary self share");

		assertFalse(Hero1118Rules.shouldSwitchStance(1, 9, 10), "stance stays before the boundary");
		assertTrue(Hero1118Rules.shouldSwitchStance(1, 10, 10), "stance switches at the boundary");
		assertTrue(Hero1118Rules.shouldSwitchStance(1, 20, 10), "stance switches at later interval boundaries");
		assertFalse(Hero1118Rules.shouldSwitchStance(0, 10, 10), "stance requires 12781");
		assertEquals(1, Hero1118Rules.nextStance(2), "golden-feather stance switches to azure-wing stance");
		assertEquals(2, Hero1118Rules.nextStance(1), "azure-wing stance switches to golden-feather stance");

		assertEquals(1, Hero1118Rules.cappedStackCount(1, 2), "stack count keeps candidates below the cap");
		assertEquals(2, Hero1118Rules.cappedStackCount(5, 2), "stack count stops at the configured cap");
		assertEquals(0, Hero1118Rules.cappedStackCount(5, 0), "zero cap disables all stacks");

		assertFalse(Hero1118Rules.isLinkedSoulEffectActive(0), "linked soul effects require their base effect");
		assertTrue(Hero1118Rules.isLinkedSoulEffectActive(1), "linked soul effects activate with their base effect");
		assertEquals(3500, Hero1118Rules.combinedEffectValue(2000, 1500), "soul effect value adds to its base effect");
		assertEquals(300000, Hero1118Rules.effectDurationMillis(300, 0), "base duration is seconds");
		assertEquals(315000, Hero1118Rules.effectDurationMillis(300, 15), "12804 extends duration in seconds");
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
}
