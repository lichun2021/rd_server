package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

public class GmCloseActivityMsg extends HawkMsg {

	public GmCloseActivityMsg() {
		super(MsgId.ACTIVITY_GM_CLOSE);
	}
	
	private int activityId;
	
	public static GmCloseActivityMsg valueOf(int activityId){
		GmCloseActivityMsg msg = new GmCloseActivityMsg();
		msg.activityId = activityId;
		return msg;
	}

	public int getActivityId() {
		return activityId;
	}
}
