package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ActivityState;
import com.hawk.gamelib.GameConst.MsgId;

public class ActivityStateChangeMsg extends HawkMsg {

	public ActivityStateChangeMsg() {
		super(MsgId.ACTIVITY_STATE_CHANGE);
	}

	/** 活动类型 */
	private ActivityType activityType;

	/** 活动期数 */
	private int termId;

	/** 活动阶段状态 */
	private ActivityState state;

	public static ActivityStateChangeMsg valueOf(ActivityType activityType, int termId, ActivityState state) {
		ActivityStateChangeMsg rewardMsg = new ActivityStateChangeMsg();

		rewardMsg.activityType = activityType;
		rewardMsg.termId = termId;
		rewardMsg.state = state;
		return rewardMsg;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
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
