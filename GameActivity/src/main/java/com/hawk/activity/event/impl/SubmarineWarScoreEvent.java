package com.hawk.activity.event.impl;

import java.util.Map;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(累计登录活动,按注册时间开启)
 * @author Jesse
 *
 */
public class SubmarineWarScoreEvent extends ActivityEvent {
	
	/** 总积分*/
	private int totalScore;
	/** 增加积分*/
	private int addScore;
	/** 通过关卡*/
	private int stage;
	/** 击杀列表*/
	private Map<Integer,Integer> killMap;
	
	public SubmarineWarScoreEvent(){ super(null);}
	public SubmarineWarScoreEvent(String playerId, int totalScore,int addScore,int stage,Map<Integer,Integer> killMap) {
		super(playerId);
		this.totalScore = totalScore;
		this.addScore = addScore;
		this.stage = stage;
		this.killMap = killMap;
	}
	
	public int getTotalScore() {
		return totalScore;
	}
	
	public int getAddScore() {
		return addScore;
	}
	
	public int getStage() {
		return stage;
	}
	
	public Map<Integer, Integer> getKillMap() {
		return killMap;
	}
}
