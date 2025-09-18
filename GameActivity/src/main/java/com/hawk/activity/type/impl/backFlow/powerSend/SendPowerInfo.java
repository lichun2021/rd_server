package com.hawk.activity.type.impl.backFlow.powerSend;

import com.alibaba.fastjson.JSONObject;

public class SendPowerInfo {

	private String playerId; 
	
	private int backType;
	
	private long startTime;
	
	private long overTime;
	

	
	public SendPowerInfo() {
	}
	
	
	public SendPowerInfo(String playerId, int backType,long startTime,long overTime) {
		super();
		this.playerId = playerId;
		this.backType = backType;
		this.startTime = startTime;
		this.overTime = overTime;
	}
	
	
	

	public String getPlayerId() {
		return playerId;
	}


	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}


	public int getBackType() {
		return backType;
	}


	public void setBackType(int backType) {
		this.backType = backType;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getOverTime() {
		return overTime;
	}


	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}


	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", this.playerId);
		obj.put("backType", this.backType);
		obj.put("startTime", this.startTime);
		obj.put("overTime", this.overTime);
		return obj.toString();
	}

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.backType = obj.getIntValue("backType");
		this.startTime = obj.getLongValue("startTime");
		this.overTime = obj.getLongValue("overTime");
	}
	
	
	
}
