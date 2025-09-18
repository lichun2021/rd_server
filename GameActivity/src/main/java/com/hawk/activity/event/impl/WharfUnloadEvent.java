package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 码头卸载货物事件
 * 
 * @author lating
 *
 */
public class WharfUnloadEvent extends ActivityEvent {
	
	public WharfUnloadEvent(){ super(null);}
	public WharfUnloadEvent(String playerId) {
		super(playerId);
	}
}
