package com.hawk.game.module.college.entity;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;

public class CollegeMemberVitEntity{
	
	
	private int sendVitCount;
	
	private long refreshTime;

	


	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	public int getSendVitCount() {
		long curTime = HawkTime.getMillisecond();
		if(HawkTime.isSameDay(this.refreshTime, curTime)){
			return this.sendVitCount;
		}else{
			return 0;
		}
	}
	
	public void addVitSend(int vit){
		long curTime = HawkTime.getMillisecond();
		if(HawkTime.isSameDay(this.refreshTime, curTime)){
			this.sendVitCount += vit;
		}else{
			this.refreshTime = curTime;
			this.sendVitCount = vit;
		}
		
	}
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("sendVitCount", this.sendVitCount);
		obj.put("refreshTime", this.refreshTime);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.sendVitCount = obj.getIntValue("sendVitCount");
		this.refreshTime = obj.getLongValue("refreshTime");
	}
}
