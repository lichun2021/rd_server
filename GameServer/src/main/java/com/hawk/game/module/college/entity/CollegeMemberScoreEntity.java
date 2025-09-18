package com.hawk.game.module.college.entity;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;

public class CollegeMemberScoreEntity {
	
	private long score;
	
	private long weekScore;
	
	private long weekRefreshTime;

	public long getScore() {
		return score;
	}
	
	public void setScore(long score) {
		this.score = score;
	}
	
	
	public long getWeekScore() {
		long curTime = HawkTime.getMillisecond();
		if(!HawkTime.isSameWeek(curTime, this.weekRefreshTime)){
			return 0;
		}
		return this.weekScore;
	}
	
	public void setWeekScore(long weekScore) {
		this.weekScore = weekScore;
	}
	
	
	public boolean addWeekScore(int scoreAdd){
		if(scoreAdd <= 0){
			return false;
		}
		long curTime = HawkTime.getMillisecond();
		if(!HawkTime.isSameWeek(curTime, this.weekRefreshTime)){
			this.weekRefreshTime = curTime;
			this.weekScore = 0;
		}
		this.weekScore += scoreAdd;
		this.score += scoreAdd;
		return true;
		
	}
	
	
	
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("score", this.score);
		obj.put("weekScore", this.weekScore);
		obj.put("weekRefreshTime", this.weekRefreshTime);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.score = obj.getLongValue("score");
		this.weekScore = obj.getLongValue("weekScore");
		this.weekRefreshTime = obj.getLongValue("weekRefreshTime");
	}
	
	

}
