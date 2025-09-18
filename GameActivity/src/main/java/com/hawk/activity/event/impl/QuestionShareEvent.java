

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享
 * 
 * @author RickMei
 *
 */
public class QuestionShareEvent  extends ActivityEvent {

	public QuestionShareEvent(){ super(null);}
	public QuestionShareEvent (String playerId) {
		super(playerId);
	}
	public static QuestionShareEvent valueOf(String playerId) {
		QuestionShareEvent pbe = new QuestionShareEvent(playerId);
		return pbe;
	}
}
