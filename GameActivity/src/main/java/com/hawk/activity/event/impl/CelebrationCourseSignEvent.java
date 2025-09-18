package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 周年历程签到
 */
public class CelebrationCourseSignEvent extends ActivityEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**次数*/
	private int n;

	public CelebrationCourseSignEvent(){ super(null);}
	public CelebrationCourseSignEvent(String playerId, int n) {
		super(playerId);
		this.n = n;
	}

	public int getN() {
		return n;
	}

}
