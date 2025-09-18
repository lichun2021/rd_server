package com.hawk.game.crossproxy.tiberiumwar;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.RpcCommonResp;

public class TiberiumEnterCallback extends CsRpcCallback {
	private Player player;
	public TiberiumEnterCallback(Player player) {
		this.player = player;
	}
	@Override
	public int invoke(Object args) {
		HawkProtocol hawkProtocol = (HawkProtocol)args;
		RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(RpcCommonResp.getDefaultInstance());
		Player.logger.info("tiberium  login_c resp  playerId:{}, errorCode:{}", player.getId(), rpcCommonResp.getErrorCode());
		if (rpcCommonResp.getErrorCode() != Status.SysError.SUCCESS_OK_VALUE) {
			TiberiumCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		
		return 0;
	}
	
	@Override
	public void onTimeout(Object obj) {
		Player.logger.info("tiberium enter timeout playerId:{}", player.getId());
		TiberiumCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
