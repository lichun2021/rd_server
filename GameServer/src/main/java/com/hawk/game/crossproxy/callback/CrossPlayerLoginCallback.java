package com.hawk.game.crossproxy.callback;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;

import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Cross.RpcCommonResp;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;

public class CrossPlayerLoginCallback extends CsRpcCallback {
	
	private Player player;
	
	public CrossPlayerLoginCallback(Player player) {
		this.player = player;
	}
	
	@Override
	public int invoke(Object args) {
		HawkProtocol hawkProtocol = (HawkProtocol) args; 
		RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(RpcCommonResp.getDefaultInstance());
		if (rpcCommonResp.getErrorCode() == 99099099) {
			Player.logger.error("cross player login failed, playerId: {}, errCode: {}", player.getId(), 99099099);
			CrossService.getInstance().removeEmigrationPlayer(player.getId());
			HPErrorCode.Builder builder = HPErrorCode.newBuilder();
			builder.setHpCode(HP.code.LOGIN_C_VALUE);
			builder.setErrCode(SysError.SERVER_BUSY_LIMIT_VALUE);
			builder.setErrFlag(0);
			HawkSession session = CrossService.getInstance().getPlayerIdSession(player.getId());
			session.sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.error("cross player login timeout, playerId: {}", player.getId());
	}

}
