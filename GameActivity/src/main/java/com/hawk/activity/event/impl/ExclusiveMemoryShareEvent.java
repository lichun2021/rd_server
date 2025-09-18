

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 在线答题分享
 * 
 * @author RickMei
 *
 */
public class ExclusiveMemoryShareEvent  extends ActivityEvent {

	public ExclusiveMemoryShareEvent(){ super(null);}
	public ExclusiveMemoryShareEvent (String playerId) {
		super(playerId);
	}
	public static ExclusiveMemoryShareEvent valueOf(String playerId) {
		ExclusiveMemoryShareEvent pbe = new ExclusiveMemoryShareEvent(playerId);
		return pbe;
	}
}
