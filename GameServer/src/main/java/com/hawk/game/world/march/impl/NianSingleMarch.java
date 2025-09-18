package com.hawk.game.world.march.impl;

import java.util.Objects;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.NianMarch;

public class NianSingleMarch extends PlayerMarch implements NianMarch {
	private BattleOutcome battleOutcome;

	public NianSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public BattleOutcome dobattle() {
		if (Objects.isNull(battleOutcome)) {
			battleOutcome = NianMarch.super.dobattle();
		}
		return battleOutcome;
	}

	@Override
	public void onMarchStart() {
		try {
			dobattle();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NIAN_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		return false;
	}

}