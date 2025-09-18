package com.hawk.game.crossproxy.callback;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CrossCallbackOperationService;
import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.RpcCommonResp;

public class StartCrossCallback extends CsRpcCallback {
	private Player player;
	public StartCrossCallback(Player player) {
		this.player = player;
	}
	@Override
	public int invoke(Object args) {
		//这里和预跨服返回的协议一样.
		HawkProtocol hawkProtocol = (HawkProtocol)args;
		RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(RpcCommonResp.getDefaultInstance());
		if (rpcCommonResp.getErrorCode() != Status.SysError.SUCCESS_OK_VALUE) {
			CrossCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		
		return 0;
	}
	
	public void onTimeout(Object args) {
		Player.logger.error("playerId:{} protocol start cross timeout ", player.getId());
		CrossCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
