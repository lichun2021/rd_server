package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.ChristmasMarch;

public class ChristmasSingleMarch extends PlayerMarch implements ChristmasMarch {

	public ChristmasSingleMarch(WorldMarch marchEntity) {
		super(marchEntity);		
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.CHRISTMAS_SINGLE;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return false;
	}

}
