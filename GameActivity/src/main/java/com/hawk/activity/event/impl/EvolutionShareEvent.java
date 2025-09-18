

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;

/**
 * 英雄进化之路分享
 * 
 * @author lating
 *
 */
public class EvolutionShareEvent extends ActivityEvent implements EvolutionEvent {
	
	public EvolutionShareEvent(){ super(null);}
	public EvolutionShareEvent(String playerId) {
		super(playerId);
	}
	
	public static EvolutionShareEvent valueOf(String playerId){
		EvolutionShareEvent event = new EvolutionShareEvent(playerId);
		return event;
	}
}
