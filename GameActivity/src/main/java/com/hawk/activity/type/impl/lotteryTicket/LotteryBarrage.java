package com.hawk.activity.type.impl.lotteryTicket;

import com.alibaba.fastjson.JSONObject;

public class LotteryBarrage {

	private String playerId;
	
	private int rewardId;
	
	private int assist;

	public LotteryBarrage() {
	}
	
	
	public LotteryBarrage(String playerId,int rewardId,int assist) {
		this.playerId = playerId;
		this.rewardId = rewardId;
		this.assist = assist;
	}
	
	
	
	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public int getRewardId() {
		return rewardId;
	}
	
	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}
	
	public void setAssist(int assist) {
		this.assist = assist;
	}
	
	public int getAssist() {
		return assist;
	}
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", this.playerId);
		obj.put("rewardId", this.rewardId);
		obj.put("assist", this.assist);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.rewardId = obj.getIntValue("rewardId");
		this.assist = obj.getIntValue("assist");
	}
	
	
	
}
