package com.hawk.game.rank.guardRank;

public class GuardRank implements Comparable<GuardRank>{
	/**
	 * 玩家的ID
	 */
	private String firstPlayerId;
	/**
	 * 第二个玩家ID.
	 */
	private String secondPlayerId;
	/**
	 * 守护值
	 */
	private int guardValue;
	/**
	 * 操作时间.
	 */
	private long operationTime;
	
	public GuardRank(String firstPlayId, String secondPlayerId, int guardValue, long operationTime) {
		this.firstPlayerId = firstPlayId;
		this.secondPlayerId = secondPlayerId;
		this.guardValue = guardValue;
		this.operationTime = operationTime;
	}
	
	@Override
	public int hashCode() {
		return firstPlayerId.hashCode() + secondPlayerId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GuardRank) {
			GuardRank guardRank = (GuardRank) obj;
			return guardRank.firstPlayerId.equals(firstPlayerId) && guardRank.secondPlayerId.equals(secondPlayerId);
		} else {
			return false;
		}
		
	}

	public String getFirstPlayerId() {
		return firstPlayerId;
	}

	public String getSecondPlayerId() {
		return secondPlayerId;
	}

	public int getGuardValue() {
		return guardValue;
	}

	public void setFirstPlayerId(String firstPlayerId) {
		this.firstPlayerId = firstPlayerId;
	}

	public void setSecondPlayerId(String secondPlayerId) {
		this.secondPlayerId = secondPlayerId;
	}

	public void setGuardValue(int guardValue) {
		this.guardValue = guardValue;
	}

	@Override
	public int compareTo(GuardRank gr) {
		if (gr.guardValue == this.guardValue) {
			return this.operationTime - gr.operationTime > 0 ? 1 : -1;
		} else {
			return gr.guardValue - this.guardValue;
		}
		
	}

	public long getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(long operationTime) {
		this.operationTime = operationTime;
	}
}
