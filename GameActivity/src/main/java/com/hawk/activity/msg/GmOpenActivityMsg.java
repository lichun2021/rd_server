package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

public class GmOpenActivityMsg extends HawkMsg {

	public GmOpenActivityMsg() {
		super(MsgId.ACTIVITY_GM_OPEN);
	}
	
	private int activityId;
	
	public static GmOpenActivityMsg valueOf(int activityId){
		GmOpenActivityMsg msg = new GmOpenActivityMsg();
		msg.activityId = activityId;
		return msg;
	}

	public int getActivityId() {
		return activityId;
	}
}
