package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
/**
 * 召唤好友.
 * @author jm
 *
 */
public class RecallFriendEvent extends ActivityEvent {

	public RecallFriendEvent(){ super(null);}
	public RecallFriendEvent(String playerId) {
		super(playerId);
	}

}
