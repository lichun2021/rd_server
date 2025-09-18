package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;
import org.hawk.os.HawkException;

import com.hawk.game.player.Player;
import com.hawk.game.service.RelationService;

public class AccountCancellationRpcInvoker extends HawkRpcInvoker {
	
	/**
	 * 玩家
	 */
	private Player player;
	

	public AccountCancellationRpcInvoker(Player player) {
		this.player = player;
	}

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkRpcMsg arg1, Map<String, Object> arg2) {
		try {
			RelationService.getInstance().deletePlayer(player.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj arg0, Map<String, Object> arg1) {
		return true;
	}
}
