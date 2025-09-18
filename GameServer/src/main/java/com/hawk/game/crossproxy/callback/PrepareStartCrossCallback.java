package com.hawk.game.crossproxy.callback;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CrossCallbackOperationService;
import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.EnterCrossMsg;
import com.hawk.game.protocol.Status;

public class PrepareStartCrossCallback extends CsRpcCallback {
	private Player player;
	private EnterCrossMsg enterCrossMsg;
	public PrepareStartCrossCallback(Player player, EnterCrossMsg enterCrossMsg) {
		this.player = player;
		this.enterCrossMsg = enterCrossMsg;
	}
	@Override
	public int invoke(Object args) {
		CrossCallbackOperationService.getInstance().onPrepareCrossBack(player, (HawkProtocol)args, enterCrossMsg);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.error("playerId:{} protocol inner enter cross timeout ", player.getId());
		CrossCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
