package com.hawk.game.crossproxy.yqzz;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.RpcCommonResp;

public class YQZZEnterCallback extends CsRpcCallback {
	private Player player;
	public YQZZEnterCallback(Player player) {
		this.player = player;
	}
	@Override
	public int invoke(Object args) {
		HawkProtocol hawkProtocol = (HawkProtocol)args;
		RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(RpcCommonResp.getDefaultInstance());
		Player.logger.info("YQZZ  login_c resp  playerId:{}, errorCode:{}", player.getId(), rpcCommonResp.getErrorCode());
		if (rpcCommonResp.getErrorCode() != Status.SysError.SUCCESS_OK_VALUE) {
			YQZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
		}
		
		return 0;
	}
	
	@Override
	public void onTimeout(Object obj) {
		Player.logger.info("yqzz enter timeout playerId:{}", player.getId());
		YQZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
