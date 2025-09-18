package com.hawk.activity.type.impl.backImmigration;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

public class BackImmgrationData {

	private String playerId;
	
	private int termId;
	
	private int backCount;
	
	private long backTime;
	
	private long logoutTime;
	
	private long power;
	
	private String targetServer;
	
	private long immgrationTime;
	
	public BackImmgrationData() {
		
	}

	
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", this.playerId);
		obj.put("termId", this.termId);
		obj.put("backCount", this.backCount);
		obj.put("backTime", this.backTime);
		obj.put("logoutTime", this.logoutTime);
		obj.put("power", this.power);
		obj.put("targetServer", this.targetServer);
		obj.put("immgrationTime", this.immgrationTime);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.termId = obj.getIntValue("termId");
		this.backCount = obj.getIntValue("backCount");
		this.backTime = obj.getLongValue("backTime");
		this.logoutTime = obj.getLongValue("logoutTime");
		this.power =  obj.getLongValue("power");
		this.targetServer = obj.getString("targetServer");
		this.immgrationTime = obj.getLongValue("immgrationTime");
	}
	
	
	
	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
	}

	public long getBackTime() {
		return backTime;
	}

	public void setBackTime(long backTime) {
		this.backTime = backTime;
	}

	public String getTargetServer() {
		return targetServer;
	}

	public void setTargetServer(String targetServer) {
		this.targetServer = targetServer;
	}

	public long getLogoutTime() {
		return logoutTime;
	}
	
	public void setLogoutTime(long logoutTime) {
		this.logoutTime = logoutTime;
	}
	
	public long getImmgrationTime() {
		return immgrationTime;
	}
	
	public void setImmgrationTime(long immgrationTime) {
		this.immgrationTime = immgrationTime;
	}
	
	public long getPower() {
		return power;
	}
	
	public void setPower(long power) {
		this.power = power;
	}
	
}
