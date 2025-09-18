package com.hawk.game.module.lianmengfgyl.march.entity;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

public class FGYLPlayerEntity {

	/** 已经获奖的期数*/
	private int rewardTerm;
	private String joinRoomId;
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("rewardTerm", this.rewardTerm);
		if(HawkOSOperator.isEmptyString(this.joinRoomId)){
			obj.put("joinRoomId", this.joinRoomId);
		}
		return obj.toJSONString();
	}

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.rewardTerm = obj.getIntValue("rewardTerm");
		if(obj.containsKey("joinRoomId")){
			this.joinRoomId = obj.getString("joinRoomId");
		}
	}
	
	
	public void setRewardTerm(int rewardTerm) {
		this.rewardTerm = rewardTerm;
	}
	
	public boolean rewardTerm(int term){
		if(this.rewardTerm == term){
			return true;
		}
		return false;
	}
	
	public boolean hasJoinRoom(String room){
		if(room.equals(this.joinRoomId)){
			return true;
		}
		return false;
	}
	
	public int getRewardTerm() {
		return rewardTerm;
	}
	
	public void setJoinRoomId(String joinRoomId) {
		this.joinRoomId = joinRoomId;
	}
	
	public String getJoinRoomId() {
		return joinRoomId;
	} 
}
