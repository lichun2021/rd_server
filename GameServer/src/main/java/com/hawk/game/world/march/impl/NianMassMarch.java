package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.march.submarch.NianMarch;

public class NianMassMarch extends PlayerMarch implements MassMarch, NianMarch {

	public NianMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NIAN_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.NIAN_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}

}
