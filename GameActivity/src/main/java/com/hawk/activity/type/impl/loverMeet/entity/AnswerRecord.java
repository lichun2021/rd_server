package com.hawk.activity.type.impl.loverMeet.entity;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

public class AnswerRecord {

	private int questId;
	private int answerId;
	private int backId;
	private int nextQuestId;
	private long answerTime;
	

	/**
	 * 序列化保存
	 */
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("questId", this.questId);
		obj.put("answerId", this.answerId);
		obj.put("backId", this.backId);
		obj.put("nextQuestId", this.nextQuestId);
		obj.put("answerTime", this.answerTime);
		return obj.toJSONString();
	}

	/**
	 * 反序列化
	 * 
	 * @param serialiedStr
	 */
	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.questId = 0;
			this.answerId = 0;
			this.backId = 0;
			this.nextQuestId = 0;
			this.answerTime = 0;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.questId = obj.getIntValue("questId");
		this.answerId = obj.getIntValue("answerId");
		this.backId = obj.getIntValue("backId");
		this.nextQuestId =  obj.getIntValue("nextQuestId");
		this.answerTime = obj.getLongValue("answerTime");
	}
	
	
	
	
	public int getQuestId() {
		return questId;
	}
	public void setQuestId(int questId) {
		this.questId = questId;
	}
	public int getAnswerId() {
		return answerId;
	}
	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}
	
	public int getNextQuestId() {
		return nextQuestId;
	}
	
	public void setNextQuestId(int nextQuestId) {
		this.nextQuestId = nextQuestId;
	}
	
	public int getBackId() {
		return backId;
	}

	public void setBackId(int backId) {
		this.backId = backId;
	}

	public long getAnswerTime() {
		return answerTime;
	}
	public void setAnswerTime(long answerTime) {
		this.answerTime = answerTime;
	}
	
	
	
	
}
