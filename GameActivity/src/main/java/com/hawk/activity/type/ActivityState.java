package com.hawk.activity.type;

import com.hawk.game.protocol.Activity;

/**
 * 活动状态
 * 
 * @author PhilChen
 *
 */
public enum ActivityState {
	/** 无状态*/
	NONE(null, Activity.ActivityState.SHOW),
	/** 显示但未开始*/
	SHOW(Activity.ActivityState.SHOW, Activity.ActivityState.OPEN),
	/** 已开始*/
	OPEN(Activity.ActivityState.OPEN, Activity.ActivityState.END),
	/** 结束但未彻底关闭*/
	END(Activity.ActivityState.END, Activity.ActivityState.HIDDEN),
	/** 隐藏 已彻底关闭*/
	HIDDEN(Activity.ActivityState.HIDDEN, Activity.ActivityState.SHOW);

	/**
	 * @param value			状态值
	 * @param nextState		下一状态
	 */
	ActivityState(Activity.ActivityState value, Activity.ActivityState nextState) {
		this.state = value;
		this.nextState = nextState;
	}

	private Activity.ActivityState state;

	private Activity.ActivityState nextState;

	public int intValue() {
		if (state == null) {
			return 0;
		}
		return state.getNumber();
	}

	public Activity.ActivityState getState() {
		return state;
	}

	public ActivityState nextState() {
		return getState(nextState);
	}

	private static ActivityState getState(Activity.ActivityState value) {
		for (ActivityState state : values()) {
			if (state.state == value) {
				return state;
			}
		}
		return NONE;
	}

	public static ActivityState getState(int value) {
		for (ActivityState state : values()) {
			if (state.intValue() == value) {
				return state;
			}
		}
		return NONE;
	}
}