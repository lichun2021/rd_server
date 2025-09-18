package com.hawk.game.data;

/**
 * 大R复仇死兵信息
 * 
 * @author lating
 *
 */
public class RevengeInfo {
	/**
	 * 角色
	 */
	private String playerId;
	/**
	 * 状态
	 */
	private RevengeState state;
	/**
	 * 起始时间
	 */
	private long startTime;
	/**
	 * 复仇商店触发时间
	 */
	private long shopStartTime;
	/**
	 * 死兵总量
	 */
	private int deadSoldierTotal;
	
	public enum RevengeState {
		INIT, // 初始期
		PREPARE, // 准备期，死兵数量达到了触发限额，但玩家不在线的情况下，先设为此状态，待玩家上线后再设为ON
		ON,   // 进行中
		END,  // 结束冷却期
	}
	
	public RevengeInfo() {
		
	}
	
	public RevengeInfo(String playerId) {
		this.playerId = playerId;
		this.state = RevengeState.INIT;
	}

	public RevengeState getState() {
		return state;
	}

	public void setState(RevengeState state) {
		this.state = state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getDeadSoldierTotal() {
		return deadSoldierTotal;
	}

	public void setDeadSoldierTotal(int deadSoldierTotal) {
		this.deadSoldierTotal = deadSoldierTotal;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getShopStartTime() {
		return shopStartTime;
	}

	public void setShopStartTime(long shopStartTime) {
		this.shopStartTime = shopStartTime;
	}

}
