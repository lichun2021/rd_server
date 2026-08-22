package com.hawk.game.battle.effect.impl.hero1120;

import com.hawk.game.battle.ISoldierbuff;

public class Buff12835 extends ISoldierbuff {
	public Buff12835(int value, int startRound, int endRound) {
		super(value, startRound, endRound);
	}

	@Override
	public boolean isActive(int round) {
		return Hero1120Rules.isBuffActive(round, getStartRound(), getEndRound());
	}
}
