package com.hawk.game.player.tick;

/**
 * player tick计时，以及其它计时器的集中统一管理
 * 
 * @author lating
 */
public class PlayerTickTimeLine {
	/**
	 * 公共tick计时（每秒tick一次）
	 */
	private long nextTickTime = 0;
	/**
	 * 玩家数据访问标识刷新
	 */
	private long dataRefreshTime;
	/**
	 * 计算玩家战力
	 */
	private long lastCalcStrengthTime;
	/**
	 * 预流失活动
	 */
	private long prestressLossTick;
	/**
	 * 手Q积分上报
	 */
	private long scoreBatchTime;
	/**
	 * 成长守护平台封禁时间
	 */
	private long careBanStartTime;
	/**
	 * 健康游戏tick时间
	 */
	private long healthGameTickTime;
	/**
	 * 周卡月卡的tick时间
	 */
	private long monthCardTickTime;
	/**
	 * 在线状态tick时间
	 */
	private long onlineTickTime;
	
	/**
	 * 健康游戏信息上报时间
	 */
	private long healthGameUpdateTime;
	/**
	 * 下一次在线时长休息提醒时间
	 */
	private long nextRemindTime;
	
	private long lastContinueOnlineTime = 0L;
	/**
	 * 二级密码失效时间
	 */
	private long secPasswdExpiryTime;
	/** 
	 * ip归属地信息 
	 */
	private long ipBelongsAddrRefreshTime = 0L;
	/**
	 * 跨服迁回时间
	 */
	private long crossBackTime;
	/**
	 * 核心勘探活动
	 */
	private long activity369Tick;
	
	
	public long getActivity369Tick() {
		return activity369Tick;
	}

	public void setActivity369Tick(long activity369Tick) {
		this.activity369Tick = activity369Tick;
	}

	public long getDataRefreshTime() {
		return dataRefreshTime;
	}
	
	public void setDataRefreshTime(long dataRefreshTime) {
		this.dataRefreshTime = dataRefreshTime;
	}
	
	public long getLastCalcStrengthTime() {
		return lastCalcStrengthTime;
	}
	
	public void setLastCalcStrengthTime(long lastCalcStrengthTime) {
		this.lastCalcStrengthTime = lastCalcStrengthTime;
	}
	
	public long getPrestressLossTick() {
		return prestressLossTick;
	}
	
	public void setPrestressLossTick(long prestressLossTick) {
		this.prestressLossTick = prestressLossTick;
	}

	public long getScoreBatchTime() {
		return scoreBatchTime;
	}

	public void setScoreBatchTime(long scoreBatchTime) {
		this.scoreBatchTime = scoreBatchTime;
	}

	public long getCareBanStartTime() {
		return careBanStartTime;
	}

	public void setCareBanStartTime(long careBanStartTime) {
		this.careBanStartTime = careBanStartTime;
	}

	public long getHealthGameTickTime() {
		return healthGameTickTime;
	}

	public void setHealthGameTickTime(long healthGameTickTime) {
		this.healthGameTickTime = healthGameTickTime;
	}

	public long getMonthCardTickTime() {
		return monthCardTickTime;
	}

	public void setMonthCardTickTime(long monthCardTickTime) {
		this.monthCardTickTime = monthCardTickTime;
	}

	public long getOnlineTickTime() {
		return onlineTickTime;
	}

	public void setOnlineTickTime(long onlineTickTime) {
		this.onlineTickTime = onlineTickTime;
	}
	
	public long getHealthGameUpdateTime() {
		return healthGameUpdateTime;
	}

	public void setHealthGameUpdateTime(long healthGameUpdateTime) {
		this.healthGameUpdateTime = healthGameUpdateTime;
	}

	public long getNextRemindTime() {
		return nextRemindTime;
	}

	public void setNextRemindTime(long nextRemindTime) {
		this.nextRemindTime = nextRemindTime;
	}
	
	public long getLastContinueOnlineTime() {
		return lastContinueOnlineTime;
	}

	public void setLastContinueOnlineTime(long lastContinueOnlineTime) {
		this.lastContinueOnlineTime = lastContinueOnlineTime;
	}
	
	public void setSecPasswdExpiryTime(long secPasswdExpiryTime) {
		this.secPasswdExpiryTime = secPasswdExpiryTime;
	}

	public long getSecPasswdExpiryTime() {
		return this.secPasswdExpiryTime;
	}

	public long getIpBelongsAddrRefreshTime() {
		return ipBelongsAddrRefreshTime;
	}

	public void setIpBelongsAddrRefreshTime(long ipBelongsAddrRefreshTime) {
		this.ipBelongsAddrRefreshTime = ipBelongsAddrRefreshTime;
	}

	public long getCrossBackTime() {
		return crossBackTime;
	}

	public void setCrossBackTime(long crossBackTime) {
		this.crossBackTime = crossBackTime;
	}

	public long getNextTickTime() {
		return nextTickTime;
	}

	public void setNextTickTime(long nextTickTime) {
		this.nextTickTime = nextTickTime;
	}
	
}
