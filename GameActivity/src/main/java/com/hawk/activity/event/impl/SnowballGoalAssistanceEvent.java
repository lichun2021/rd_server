package com.hawk.activity.event.impl;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.event.ActivityEvent;

/**
 * 雪球大战进球助攻事件
 * @author golden
 *
 */
public class SnowballGoalAssistanceEvent extends ActivityEvent {

	private int x;
	
	private int y;
	
	private int pointType;
	
	private List<String> record;
	
	private int number;
	
	public SnowballGoalAssistanceEvent(){ super(null);}
	public SnowballGoalAssistanceEvent(String playerId, int x, int y, int pointType, List<String> record, int number) {
		super(playerId);
		this.x = x;
		this.y = y;
		this.pointType = pointType;
		this.record = new ArrayList<>(record);
		this.number = number;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getPointType() {
		return pointType;
	}

	public void setPointType(int pointType) {
		this.pointType = pointType;
	}

	public List<String> getRecord() {
		return record;
	}

	public void setRecord(List<String> record) {
		this.record = record;
	}
	
	public int getPointId() {
		return (y << 16) | x;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
