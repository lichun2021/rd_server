package com.hawk.game.lianmengxzq.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.lianmengxzq.XZQService;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class XZQBuildUpdateInvoker extends HawkMsgInvoker {
	
	int pointId;
	
	public XZQBuildUpdateInvoker(int pointId) {
		this.pointId = pointId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		XZQService.getInstance().addUpdatePoint(pointId);
		return true;
	}



}
