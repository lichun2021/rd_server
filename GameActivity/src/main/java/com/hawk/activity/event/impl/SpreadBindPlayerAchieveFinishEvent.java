

package com.hawk.activity.event.impl;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 推广员 活动 绑定的玩家完成成就
 * 
 * @author RickMei
 *
 */
public class SpreadBindPlayerAchieveFinishEvent extends ActivityEvent {
	
	private String friendOpenId;
	private String friendServerId;
	private String friendPlayerId; //即玩家id
	private int friendCityLevel;
	private int friendVipLevel;
	private List<Integer> friendAchieveIds = new ArrayList<>();
	private String achieveStr;
	public String getFriendOpenId(){
		return this.friendOpenId;
	}
	
	public String getFriendServerId(){
		return this.friendServerId;
	}
	
	public String getFriendPlayerId(){
		return this.friendPlayerId;
	}
	public String getAchieveStr(){
		return this.achieveStr;
	}
	public int getFriendCityLevel(){
		return this.friendCityLevel;
	}
	
	public int getFriendVipLevel(){
		return this.friendVipLevel;
	}
	
	public List<Integer> getFriendAchieveIds(){
		return this.friendAchieveIds;
	}
	
	public SpreadBindPlayerAchieveFinishEvent(){ super(null);}
	public SpreadBindPlayerAchieveFinishEvent (String playerId, String friendOpenId, String friendServerId, String friendPlayerId, int friendCityLevel, int friendVipLevel, String achieveStr) {
		super(playerId);
		this.friendOpenId = friendOpenId;
		this.friendServerId = friendServerId;
		this.friendPlayerId = friendPlayerId;
		this.friendCityLevel = friendCityLevel;
		this.friendVipLevel = friendVipLevel;
		this.achieveStr = achieveStr;
		this.friendAchieveIds = SerializeHelper.stringToList(Integer.class, achieveStr, "_"); 
	}

	public static SpreadBindPlayerAchieveFinishEvent valueOf(String playerId, String friendOpenId, String friendServerId, String friendPlayerId, int friendCityLevel, int friendVipLevel, String achieveStr) {
		SpreadBindPlayerAchieveFinishEvent pbe = new SpreadBindPlayerAchieveFinishEvent(playerId, friendOpenId, friendServerId, friendPlayerId, friendCityLevel, friendVipLevel, achieveStr);
		return pbe;
	}
} 
