package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

public class ChristmasMassJoinMarch extends PassiveMarch implements MassJoinMarch {

	public ChristmasMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CHRISTMAS_MASS_JOIN;
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
	}
}