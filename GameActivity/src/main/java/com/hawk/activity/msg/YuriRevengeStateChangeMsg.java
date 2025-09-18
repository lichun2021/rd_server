package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.gamelib.GameConst.MsgId;

public class YuriRevengeStateChangeMsg extends HawkMsg {

	public YuriRevengeStateChangeMsg() {
		super(MsgId.YURIREVENGE_STATE_CHANGE);
	}
	
	private int termId;
	private ActivityState state;
	
	public static YuriRevengeStateChangeMsg valueOf(int termId, ActivityState state){
		YuriRevengeStateChangeMsg rewardMsg = new YuriRevengeStateChangeMsg();
		rewardMsg.termId = termId;
		rewardMsg.state = state;
		return rewardMsg;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public ActivityState getState() {
		return state;
	}

	public void setState(ActivityState state) {
		this.state = state;
	}

	
	
}
