

package com.hawk.activity.event.impl;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 推广员 活动 绑定的玩家主堡等级提升
 * 
 * @author RickMei
 *
 */
public class SpreadOperatorRetEvent  extends ActivityEvent {
	private String op;
	private boolean ret;
	private String code; //即玩家id
	private List<Integer> achieveIds = new ArrayList<>();
	private String achieveStr;
	public String getCode(){
		return this.code;
	}
	
	public List<Integer> getAchieveIds(){
		return this.achieveIds;
	}
	
	public String getAchieveStr(){
		return this.achieveStr;
	}
	public String getOp(){
		return this.op;
	}
	public boolean getRet(){
		return this.ret;
	}
	
	public SpreadOperatorRetEvent(){ super(null);}
	public SpreadOperatorRetEvent (String op, String ret, String playerId, String friendPlayerId, String achieveStr) {
		super(playerId);
		this.code = friendPlayerId;
		this.achieveStr = achieveStr;
		this.achieveIds = SerializeHelper.stringToList(Integer.class, achieveStr, "_"); 
		this.op = op;
		this.ret = Boolean.valueOf(ret);
	}
	
	public String toString(){
		return String.format("player=%s,op=%s,ret=%s,code=%s,achieve=%s", getPlayerId(), op, String.valueOf(ret),code,achieveStr);
	}

}
