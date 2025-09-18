package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 体力消耗
 * @author Golden
 *
 * 新加成就任务,防刷。行军到达目标点,成功攻击以后才计算体力消耗。
 */
public class VitCostEvent extends ActivityEvent {

	/**
	 * 体力消耗
	 */
	private int cost;
	/**
	 * 是否是集结消耗
	 */
	private boolean mass;

	public VitCostEvent(){ super(null);}
	public VitCostEvent(String playerId, int cost) {
		super(playerId);
		this.cost = cost;
	}
	
	public VitCostEvent(String playerId, int cost, boolean mass) {
		super(playerId);
		this.cost = cost;
		this.mass = mass;
	}

	public int getCost() {
		return cost;
	}
	
	public boolean isMass() {
		return mass;
	}
}
