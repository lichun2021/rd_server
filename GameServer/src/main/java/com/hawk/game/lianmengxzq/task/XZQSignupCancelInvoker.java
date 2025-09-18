package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XZQ.PBXZQCancelSignupReq;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQSignupCancelInvoker extends HawkMsgInvoker {
	Player player;
	int buildId;
	PBXZQCancelSignupReq req;

	
	public XZQSignupCancelInvoker(Player player,int buildId,PBXZQCancelSignupReq req) {
		this.player = player;
		this.buildId = buildId;
		this.req = req;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().onCancelSingup(player, buildId,req);
		return true;
	}



}
