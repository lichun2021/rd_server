package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;

public class PlayerAccountResetInvoker extends HawkMsgInvoker {
	
	private Player player;
	public int code;
	
	public PlayerAccountResetInvoker(Player player, int code) {
		this.player = player;
		this.code = code;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GameUtil.resetAccount(player, code);
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
	public int getCode() {
		return code;
	}

}
