package com.hawk.game.crossproxy.cyborg;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.CyborgCrossMsg;

public class CyborgPrepareEnterCallback extends CsRpcCallback {
	private Player player;
	private CyborgCrossMsg crossMsg;
	public CyborgPrepareEnterCallback(Player player, CyborgCrossMsg crossMsg) {
		this.player = player;
		this.crossMsg = crossMsg;
	}
	
	@Override
	public int invoke(Object args) { 
		CyborgCallbackOperationService.getInstance().onPrepareCrossBack(player, (HawkProtocol)args, crossMsg);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.info("cyborg prepare enter timeout playerId:{}", player.getId());
		CyborgCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
