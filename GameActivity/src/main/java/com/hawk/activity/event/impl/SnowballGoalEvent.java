package com.hawk.activity.event.impl;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.event.ActivityEvent;

/**
 * 雪球大战进球事件
 * @author golden
 *
 */
public class SnowballGoalEvent extends ActivityEvent {

	private int x;
	
	private int y;
	
	private int pointType;
	
	private List<String> record;
	
	public SnowballGoalEvent(){ super(null);}
	public SnowballGoalEvent(String playerId, int x, int y, int pointType, List<String> record) {
		super(playerId);
		this.x = x;
		this.y = y;
		this.pointType = pointType;
		this.record = new ArrayList<>(record);
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
}
