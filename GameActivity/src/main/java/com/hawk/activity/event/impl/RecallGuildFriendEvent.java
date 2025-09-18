package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 召唤联盟好友.
 * @author hf
 *
 */
public class RecallGuildFriendEvent extends ActivityEvent {

	public RecallGuildFriendEvent(){ super(null);}
	public RecallGuildFriendEvent(String playerId) {
		super(playerId);
	}

}
