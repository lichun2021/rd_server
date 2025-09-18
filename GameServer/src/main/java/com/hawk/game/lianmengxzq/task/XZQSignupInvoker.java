package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XZQ.PBXZQSignupReq;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQSignupInvoker extends HawkMsgInvoker {
	
	Player player;
	
	int buildingId;
	
	PBXZQSignupReq req;

	
	public XZQSignupInvoker(Player player,int buildingId,PBXZQSignupReq req) {
		this.player = player;
		this.buildingId = buildingId;
		this.req = req;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().onPlayerSignup(player, buildingId,req);
		return true;
	}



}
