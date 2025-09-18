package com.hawk.game.module.spacemecha.worldmarch;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.MassJoinMarch;

public class SpaceMechaStrongHoldMassJoinMarch extends PassiveMarch implements MassJoinMarch {

	public SpaceMechaStrongHoldMassJoinMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN;
	}

	@Override
	public void detailMarchStop(WorldPoint targetPoint) {
		MassJoinMarch.super.detailMarchStop(targetPoint);
	}
}