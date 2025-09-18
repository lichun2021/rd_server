package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.protocol.Activity.ActivityState;

public class YuriRevengeStageChangeMsgInvoker extends HawkMsgInvoker {
	
	private int termId;
	
	private ActivityState state;
	
	public YuriRevengeStageChangeMsgInvoker(ActivityState state, int termId) {
		this.state = state;
		this.termId = termId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		switch (state.getNumber()) {
		case ActivityState.SHOW_VALUE:
			YuriRevengeService.getInstance().onActivityShow(termId);
			break;
		case ActivityState.END_VALUE:
			YuriRevengeService.getInstance().onActivityEnd(termId);
			break;
		default:
			break;
		}
		return true;
	}

	public int getTermId() {
		return termId;
	}

	public ActivityState getState() {
		return state;
	}
}
