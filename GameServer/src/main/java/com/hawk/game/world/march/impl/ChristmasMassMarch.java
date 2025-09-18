package com.hawk.game.world.march.impl;


import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.ChristmasMarch;
import com.hawk.game.world.march.submarch.MassMarch;

public class ChristmasMassMarch extends PlayerMarch implements MassMarch, ChristmasMarch {

	public ChristmasMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CHRISTMAS_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.CHRISTMAS_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
}
