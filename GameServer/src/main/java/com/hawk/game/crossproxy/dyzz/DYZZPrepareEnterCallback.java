package com.hawk.game.crossproxy.dyzz;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.DYZZCrossMsg;
import com.hawk.game.protocol.Status;

public class DYZZPrepareEnterCallback extends CsRpcCallback {
	private Player player;
	private DYZZCrossMsg crossMsg;
	public DYZZPrepareEnterCallback(Player player, DYZZCrossMsg crossMsg) {
		this.player = player;
		this.crossMsg = crossMsg;
	}
	
	
	@Override
	public int invoke(Object args) { 
		DYZZCallbackOperationService.getInstance().onPrepareCrossBack(player, (HawkProtocol)args, crossMsg);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onTimeout(Object args) {
		Player.logger.info("dyzz prepare enter timeout playerId:{}", player.getId());
		DYZZCallbackOperationService.getInstance().onPrepareCrossFail(player);
	}

}
