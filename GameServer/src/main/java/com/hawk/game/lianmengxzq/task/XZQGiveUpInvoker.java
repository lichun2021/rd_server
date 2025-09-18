package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XZQ.PBXZQGiveUpReq;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQGiveUpInvoker extends HawkMsgInvoker {
	
	Player player;
	
	int buildingId;
	
	PBXZQGiveUpReq req;

	
	public XZQGiveUpInvoker(Player player,int buildingId,PBXZQGiveUpReq req) {
		this.player = player;
		this.buildingId = buildingId;
		this.req = req;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().onGiveupPoint(player, buildingId);
		return true;
	}



}
