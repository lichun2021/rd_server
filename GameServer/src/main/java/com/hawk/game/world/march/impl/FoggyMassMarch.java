package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.PushService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.FoggyMarch;
import com.hawk.game.world.march.submarch.MassMarch;


/**
 * 幽灵基地集结行军
 * @author golden
 *
 */
public class FoggyMassMarch extends PlayerMarch implements MassMarch, FoggyMarch {

	public FoggyMassMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS;
	}

	@Override
	public WorldMarchType getJoinMassType() {
		return WorldMarchType.FOGGY_FORTRESS_MASS_JOIN;
	}

	@Override
	public boolean needShowInGuildWar() {
		return true;
	}
	
	@Override
	public void remove() {
		super.remove();
		String playerId = getMarchEntity().getPlayerId();
		if((this.getMarchEntity().getMarchProcMask() & GsConst.MarchProcMask.IS_MARCHREACH) > 0) {
			PushService.getInstance().pushMsg(playerId, PushMsgType.ATTACK_FOGGY_ARMY_RETURN_VALUE);
		}
	}
}
