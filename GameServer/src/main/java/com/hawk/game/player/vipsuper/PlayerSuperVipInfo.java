package com.hawk.game.player.vipsuper;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家至尊vip信息
 * 
 * @author lating
 *
 */
public class PlayerSuperVipInfo {
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 实际至尊vip等级
	 */
	private int actualLevel;
	
	/**
	 * 当月已激活的等级
	 */
	private int activatedLevel;
	/**
	 * 当月至尊积分
	 */
	private int monthVipScore;
	/**
	 * 日活跃奖励积分领取时间
	 */
	private long dailyActiveRecieveTime;
	/**
	 * 上一次登录的奖励积分
	 */
	private int lastLoginScore;
	/**
	 * 每日登录奖励积分领取时间
	 */
	private long dailyLoginRecieveTime;
	/**
	 * 每日礼包领取时间
	 */
	private long dailyGiftRecieveTime;
	/**
	 * 当前激活周期对应的自然月
	 */
	private long activatedPeriodMonth;
	/**
	 * 已领取的月度礼包
	 */
	private List<Integer> monthGiftRecieved;
	/**
	 * 连续登录天数
	 */
	private int loginDays;
	/**
	 * 最近一次登录时间
	 */
	private long loginTime;
	/**
	 * 皮肤特效是否已激活：大于0时表示激活了对应等级的皮肤特效
	 */
	private int skinEffActivated;
	/**
	 * 手动激活期结束时间：-1表示自动激活（当月一直有效）
	 */
	private long activeEndTime = -1;
	
	public PlayerSuperVipInfo() {
		monthGiftRecieved = new ArrayList<Integer>();
	}
	
	public PlayerSuperVipInfo(String playerId) {
		this.playerId = playerId;
		monthGiftRecieved = new ArrayList<Integer>();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getActualLevel() {
		return actualLevel;
	}

	public void setActualLevel(int actualLevel) {
		this.actualLevel = actualLevel;
	}

	public int getActivatedLevel() {
		return activatedLevel;
	}

	public void setActivatedLevel(int activatedLevel) {
		this.activatedLevel = activatedLevel;
	}

	public int getMonthVipScore() {
		return monthVipScore;
	}

	public void setMonthVipScore(int monthVipScore) {
		this.monthVipScore = monthVipScore;
	}

	public long getDailyActiveRecieveTime() {
		return dailyActiveRecieveTime;
	}

	public void setDailyActiveRecieveTime(long dailyActiveRecieveTime) {
		this.dailyActiveRecieveTime = dailyActiveRecieveTime;
	}

	public long getDailyLoginRecieveTime() {
		return dailyLoginRecieveTime;
	}

	public void setDailyLoginRecieveTime(long dailyLoginRecieveTime) {
		this.dailyLoginRecieveTime = dailyLoginRecieveTime;
	}

	public long getDailyGiftRecieveTime() {
		return dailyGiftRecieveTime;
	}

	public void setDailyGiftRecieveTime(long dailyGiftRecieveTime) {
		this.dailyGiftRecieveTime = dailyGiftRecieveTime;
	}

	public List<Integer> getMonthGiftRecieved() {
		return monthGiftRecieved;
	}

	public void setMonthGiftRecieved(List<Integer> monthGiftRecieved) {
		this.monthGiftRecieved = monthGiftRecieved;
	}

	public int getLastLoginScore() {
		return lastLoginScore;
	}

	public void setLastLoginScore(int lastLoginScore) {
		this.lastLoginScore = lastLoginScore;
	}

	public long getActivatedPeriodMonth() {
		return activatedPeriodMonth;
	}

	public void setActivatedPeriodMonth(long activatedPeriodMonth) {
		this.activatedPeriodMonth = activatedPeriodMonth;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	
	public int getSkinEffActivated() {
		return skinEffActivated;
	}

	public void setSkinEffActivated(int skinEffActivated) {
		this.skinEffActivated = skinEffActivated;
	}
	
	public long getActiveEndTime() {
		return activeEndTime;
	}

	public void setActiveEndTime(long activeEndTime) {
		this.activeEndTime = activeEndTime;
	}

}
