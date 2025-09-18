package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享成功
 * 
 * @author RickMei
 *
 */
public class QuestionShareOkEvent extends ActivityEvent {

	public QuestionShareOkEvent(){ super(null);}
	public QuestionShareOkEvent(String playerId) {
		super(playerId);
	}

	public static QuestionShareOkEvent valueOf(String playerId) {
		QuestionShareOkEvent pbe = new QuestionShareOkEvent(playerId);
		return pbe;
	}

}
