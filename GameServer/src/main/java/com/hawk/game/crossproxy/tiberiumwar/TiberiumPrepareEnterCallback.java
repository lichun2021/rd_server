package com.hawk.game.crossproxy.tiberiumwar;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.TiberiumCrossMsg;

public class TiberiumPrepareEnterCallback extends CsRpcCallback {
	private Player player;
	private TiberiumCrossMsg crossMsg;
	public TiberiumPrepareEnterCallback(Player player, TiberiumCrossMsg crossMsg) {
		this.player = player;
		this.crossMsg = crossMsg;
	}
	
	
	@Override
	public int invoke(Object args) { 
		TiberiumCallbackOperationService.getInstance().onPrepareCrossBack(player, (HawkProtocol)args, crossMsg);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.info("tiberium prepare enter timeout playerId:{}", player.getId());
		TiberiumCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
