package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;

/**
 * 周年历程
 * 
 */
public class CelebrationCourseShareEvent extends ActivityEvent implements EvolutionEvent {
	
	private static final long serialVersionUID = 1L;

	public CelebrationCourseShareEvent(){ super(null);}
	public CelebrationCourseShareEvent(String playerId) {
		super(playerId);
	}
	
	public static CelebrationCourseShareEvent valueOf(String playerId){
		CelebrationCourseShareEvent event = new CelebrationCourseShareEvent(playerId);
		return event;
	}
}
