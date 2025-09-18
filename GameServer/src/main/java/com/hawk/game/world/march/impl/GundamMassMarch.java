package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.GundamMarch;
import com.hawk.game.world.march.submarch.MassMarch;

/**
 * 高达集结行军
 * @author golden
 *
 */
public class GundamMassMarch extends PlayerMarch implements MassMarch, GundamMarch {

	public GundamMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.GUNDAM_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.GUNDAM_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
}
