package com.hawk.activity.type.impl.backFlow.powerSend;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;

public class PowerMessage {

	private String messageId; 
	
	private String sender;
	
	private String reviever;
	
	private String openRewards;
	
	private String backRewards;
	
	private long achieveRewardsTime;
	
	private long backTime;
	
	private int state;
	
	private long outTime;
	
	public PowerMessage() {
	}
	
	
	public PowerMessage(String messageId, String sender, String reviever,String openRewards,String backRewards,long outTime) {
		super();
		this.messageId = messageId;
		this.sender = sender;
		this.reviever = reviever;
		this.openRewards = openRewards;
		this.backRewards = backRewards;
		this.outTime = outTime;
		
	}
	
	/**
	 * 是否过期
	 * @return
	 */
	public boolean isOutTime(){
		long curTime = HawkTime.getMillisecond();
		return curTime > this.outTime;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReviever() {
		return reviever;
	}

	public void setReviever(String reviever) {
		this.reviever = reviever;
	}
	
	
	
	public int getState() {
		return state;
	}




	public void setState(int state) {
		this.state = state;
	}

	

	

	


	

	public String getOpenRewards() {
		return openRewards;
	}


	public void setOpenRewards(String openRewards) {
		this.openRewards = openRewards;
	}


	public String getBackRewards() {
		return backRewards;
	}


	public void setBackRewards(String backRewards) {
		this.backRewards = backRewards;
	}
	
	


	public long getAchieveRewardsTime() {
		return achieveRewardsTime;
	}


	public void setAchieveRewardsTime(long achieveRewardsTime) {
		this.achieveRewardsTime = achieveRewardsTime;
	}


	public long getBackTime() {
		return backTime;
	}


	public void setBackTime(long backTime) {
		this.backTime = backTime;
	}

	
	

	public long getOutTime() {
		return outTime;
	}


	public void setOutTime(long outTime) {
		this.outTime = outTime;
	}


	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("messageId", this.messageId);
		obj.put("sender", this.sender);
		obj.put("reviever", this.reviever);
		obj.put("openRewards", this.openRewards);
		obj.put("backRewards", this.backRewards);
		obj.put("achieveRewardsTime", this.achieveRewardsTime);
		obj.put("backTime", this.backTime);
		obj.put("state", this.state);
		obj.put("outTime", this.outTime);
		return obj.toString();
	}

	
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.messageId = obj.getString("messageId");
		this.sender = obj.getString("sender");
		this.reviever = obj.getString("reviever");
		this.openRewards = obj.getString("openRewards");
		this.backRewards = obj.getString("backRewards");
		this.achieveRewardsTime = obj.getLongValue("achieveRewardsTime");
		this.backTime = obj.getLongValue("backTime");
		this.state = obj.getIntValue("state");
		this.outTime = obj.getLongValue("outTime");
	}
	
	
	
	
}
