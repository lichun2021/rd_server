

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 推广员活动玩家绑定 推广码成功
 * 
 * @author RickMei
 *
 */
public class SpreadBindCodeSuccEvent  extends ActivityEvent {
	
	private String friendOpenId;
	private String friendServerId;
	private String friendPlayerId; //即玩家id
	private int friendCityLevel;
	private int friendVipLevel;
	public String getFriendOpenId(){
		return this.friendOpenId;
	}
	
	public String getFriendServerId(){
		return this.friendServerId;
	}
	
	public String getFriendPlayerId(){
		return this.friendPlayerId;
	}
	
	public int getFriendCityLevel(){
		return this.friendCityLevel;
	}
	
	public int getFriendVipLevel(){
		return this.friendVipLevel;
	}
	
	public SpreadBindCodeSuccEvent(){ super(null);}
	public SpreadBindCodeSuccEvent (String playerId, String friendOpenId, String friendServerId, String friendPlayerId, int friendCityLevel, int friendVipLevel) {
		super(playerId);
		this.friendOpenId = friendOpenId;
		this.friendServerId = friendServerId;
		this.friendPlayerId = friendPlayerId;
		this.friendCityLevel = friendCityLevel;
		this.friendVipLevel = friendVipLevel;
	}
	public static SpreadBindCodeSuccEvent valueOf(String playerId, String friendOpenId, String friendServerId, String friendPlayerId, int friendCityLevel, int friendVipLevel) {
		SpreadBindCodeSuccEvent pbe = new SpreadBindCodeSuccEvent(playerId, friendOpenId, friendServerId, friendPlayerId, friendCityLevel, friendVipLevel);
		return pbe;
	}
}
