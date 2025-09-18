package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.player.Player;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQFoceColorSetCancelInvoker extends HawkMsgInvoker {
	Player player;
	int colorId;
	

	
	public XZQFoceColorSetCancelInvoker(Player player,int colorId) {
		this.player = player;
		this.colorId = colorId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().setGuildForceColor(player,colorId);
		return true;
	}



}
