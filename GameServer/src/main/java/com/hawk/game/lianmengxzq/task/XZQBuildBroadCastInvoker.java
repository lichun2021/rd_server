package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;

public class XZQBuildBroadCastInvoker extends HawkMsgInvoker {
	
	
	public XZQBuildBroadCastInvoker() {
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().broadcastXZQInfo(null);
		return true;
	}



}
