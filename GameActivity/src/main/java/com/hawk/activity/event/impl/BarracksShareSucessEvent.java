

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 兵营分享
 * @author che
 *
 */
public class BarracksShareSucessEvent  extends ActivityEvent {

	
	public BarracksShareSucessEvent(){ super(null);}
	public BarracksShareSucessEvent (String playerId) {
		super(playerId);
	}

	public static BarracksShareSucessEvent valueOf(String playerId){
		BarracksShareSucessEvent event = new BarracksShareSucessEvent(playerId);
		return event;
	}
	
	
}
