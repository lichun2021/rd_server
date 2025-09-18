

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 威龙庆典分享
 * 
 * @author lating
 *
 */
public class WLQDShareEvent extends ActivityEvent{
	
	public WLQDShareEvent(){ super(null);}
	public WLQDShareEvent(String playerId) {
		super(playerId);
	}
	
	public static WLQDShareEvent valueOf(String playerId){
		WLQDShareEvent event = new WLQDShareEvent(playerId);
		return event;
	}
}
