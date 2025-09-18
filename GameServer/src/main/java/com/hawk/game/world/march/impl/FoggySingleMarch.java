package com.hawk.game.world.march.impl;

import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.PushService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.FoggyMarch;

/**
 * 迷雾要赛单人行军
 * @author golden
 *
 */
public class FoggySingleMarch extends PlayerMarch implements FoggyMarch{

	public FoggySingleMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.FOGGY_SINGLE;
	}
	
	@Override
	public boolean needShowInGuildWar() {
		return false;
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
