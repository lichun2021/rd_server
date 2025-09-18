package com.hawk.game.crossproxy.yqzz;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.YQZZCrossMsg;
import com.hawk.game.protocol.Status;

public class YQZZPrepareEnterCallback extends CsRpcCallback {
	private Player player;
	private YQZZCrossMsg crossMsg;
	public YQZZPrepareEnterCallback(Player player, YQZZCrossMsg crossMsg) {
		this.player = player;
		this.crossMsg = crossMsg;
	}
	
	
	@Override
	public int invoke(Object args) { 
		YQZZCallbackOperationService.getInstance().onPrepareCrossBack(player, (HawkProtocol)args, crossMsg);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.info("yqzz prepare enter timeout playerId:{}", player.getId());
		YQZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
