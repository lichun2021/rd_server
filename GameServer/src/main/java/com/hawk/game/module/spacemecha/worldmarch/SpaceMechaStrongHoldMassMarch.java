package com.hawk.game.module.spacemecha.worldmarch;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.MassMarch;

public class SpaceMechaStrongHoldMassMarch extends PlayerMarch implements MassMarch, SpaceMechaStrongHoldMarch {

	public SpaceMechaStrongHoldMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	@Override
	public long getMarchNeedTime() {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		return cfg.getMarchTime();
	}

}
