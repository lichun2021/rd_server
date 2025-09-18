package com.hawk.game.module.spacemecha.worldmarch;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;

public class SpaceMechaStrongHoldSingleMarch extends PlayerMarch implements SpaceMechaStrongHoldMarch {

	public SpaceMechaStrongHoldSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE;
	}

	@Override
	public boolean needShowInGuildWar() {
		return false;
	}
	
	@Override
	public long getMarchNeedTime() {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		return cfg.getMarchTime();
	}

}