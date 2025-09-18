package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;

public class TreasureHuntMonsterMassJoinMarch extends MassMonsterJoinMarch {

	public TreasureHuntMonsterMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN;
	}
}
