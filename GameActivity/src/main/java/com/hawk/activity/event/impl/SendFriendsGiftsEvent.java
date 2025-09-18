package com.hawk.activity.event.impl;

import java.util.Set;

import com.hawk.activity.event.ActivityEvent;

/***
 * 好友赠送事件
 * @author che
 *
 */
public class SendFriendsGiftsEvent extends ActivityEvent {

	private Set<String> friends;
	
	private boolean inGameFriend;
	
	public SendFriendsGiftsEvent(){ super(null);}
	public SendFriendsGiftsEvent(String playerId, Set<String> friends,boolean inGameFriend) {
		super(playerId, true);
		this.friends = friends;
		this.inGameFriend = inGameFriend;
	}


	public Set<String> getFriends() {
		return friends;
	}


	public void setFriends(Set<String> friends) {
		this.friends = friends;
	}


	public boolean isInGameFriend() {
		return inGameFriend;
	}


	public void setInGameFriend(boolean inGameFriend) {
		this.inGameFriend = inGameFriend;
	}


	
	
	

	
}
