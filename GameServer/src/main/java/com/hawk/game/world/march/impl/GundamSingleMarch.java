package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.GundamMarch;

/**
 * 高达单人行军
 * @author golden
 *
 */
public class GundamSingleMarch extends PlayerMarch implements GundamMarch {

	public GundamSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.GUNDAM_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		return false;
	}
}
