package com.hawk.game.entity; 

import javax.persistence.Column;

import com.alibaba.fastjson.annotation.JSONField;


public class PlayerRelationApplyEntity{

	/**申请者的ID*/
	@JSONField(serialize = false)
	private String playerId;

	/**被申请的玩家ID*/
	@JSONField(serialize = false)
	private String targetPlayerId;

	/**申请介绍*/
	private String content;

	/**申请时间*/
	@Column(name="applyTime", nullable = false)
	private int applyTime;

	public String getPlayerId(){
		return this.playerId; 
	}

	public void setPlayerId(String playerId){
		this.playerId = playerId;
	}

	public String getTargetPlayerId(){
		return this.targetPlayerId; 
	}

	public void setTargetPlayerId(String targetPlayerId){
		this.targetPlayerId = targetPlayerId;
	}

	public String getContent(){
		return this.content; 
	}

	public void setContent(String content){
		this.content = content;
	}

	public int getApplyTime(){
		return this.applyTime; 
	}

	public void setApplyTime(int applyTime){
		this.applyTime = applyTime;
	}
}