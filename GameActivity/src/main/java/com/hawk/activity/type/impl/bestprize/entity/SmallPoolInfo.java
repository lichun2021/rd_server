package com.hawk.activity.type.impl.bestprize.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个小奖池中包含的奖励信息
 */
public class SmallPoolInfo {
	/**
	 * 小奖池id
	 */
	private int poolId;
	/**
	 * 总共抽了多少次
	 */
	private int drawTimesTotal;
	/**
	 * 剩余A奖数据
	 */
	private int remainRewardA;
	/**
	 * 每个奖励抽了多少次
	 */
	private Map<Integer, Integer> awardDrawMap = new HashMap<>();
	
	public SmallPoolInfo() {}
	
	public SmallPoolInfo(int poolId, int remainRewardA) {
		this.poolId = poolId;
		this.remainRewardA = remainRewardA;
	}

	public int getPoolId() {
		return poolId;
	}

	public void setPoolId(int poolId) {
		this.poolId = poolId;
	}

	public int getDrawTimesTotal() {
		return drawTimesTotal;
	}

	public void setDrawTimesTotal(int drawTimesTotal) {
		this.drawTimesTotal = drawTimesTotal;
	}

	public Map<Integer, Integer> getAwardDrawMap() {
		return awardDrawMap;
	}

	public void setAwardDrawMap(Map<Integer, Integer> awardDrawMap) {
		this.awardDrawMap = awardDrawMap;
	}

	public int getRemainRewardA() {
		return remainRewardA;
	}

	public void setRemainRewardA(int remainRewardA) {
		this.remainRewardA = remainRewardA;
	}
	
}
