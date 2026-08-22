package com.hawk.game.battle.effect.impl.hero1116;

public final class Hero1116RulesHarness {
	private Hero1116RulesHarness() {
	}

	public static void main(String[] args) {
		assertFalse(Hero1116Rules.is12721Active(0), "12721 requires a positive effect");
		assertTrue(Hero1116Rules.is12721Active(1), "12721 activates for a positive effect");

		assertFalse(Hero1116Rules.is12722Round(0, 5, 5), "12722 requires its effect");
		assertFalse(Hero1116Rules.is12722Round(1, 4, 5), "12722 skips non-modulo rounds");
		assertTrue(Hero1116Rules.is12722Round(1, 5, 5), "12722 runs on modulo rounds");

		assertTrue(Hero1116Rules.is12729Active(1, 25, 25), "12729 includes its last round");
		assertFalse(Hero1116Rules.is12729Active(1, 26, 25), "12729 excludes rounds after its boundary");
		assertFalse(Hero1116Rules.is12730Active(1, 9, 10), "12730 excludes rounds before its boundary");
		assertTrue(Hero1116Rules.is12730Active(1, 10, 10), "12730 includes its first round");

		assertTrue(Hero1116Rules.isBothSelfFight(true, true), "12729/12730 require both troops to be self-fight");
		assertFalse(Hero1116Rules.isBothSelfFight(true, false), "12729/12730 reject a non-self-fight target");
		assertFalse(Hero1116Rules.isBothSelfFight(false, true), "12729/12730 reject a non-self-fight source");
		assertFalse(Hero1116Rules.isBothSelfFight(false, false), "12729/12730 reject two non-self-fight troops");

		assertEquals(300000, Hero1116Rules.effectDurationMillis(300, 0), "base duration is seconds");
		assertEquals(315000, Hero1116Rules.effectDurationMillis(300, 15), "12746 soul duration extends base seconds");
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
