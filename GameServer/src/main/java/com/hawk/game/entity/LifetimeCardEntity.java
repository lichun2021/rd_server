package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

/**
 * 终身卡
 * @author Golden
 *
 */
@Entity
@Table(name = "lifetime_card")
public class LifetimeCardEntity extends HawkDBEntity {
	
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId = "";
	
	/**
	 * 普通卡解锁时间
	 */
	@Column(name = "commonUnlockTime", nullable = false)
	@IndexProp(id = 2)
	private long commonUnlockTime;
	
	/**
	 * 进阶卡结束时间
	 */
	@Column(name = "advancedEndTime", nullable = false)
	@IndexProp(id = 3)
	private long advancedEndTime;
	
	/**
	 * 每周奖励领取次数
	 */
	@Column(name = "weekAwardTime", nullable = false)
	@IndexProp(id = 4)
	protected long weekAwardTime = 0;
	
	/**
	 * 每月奖励领取次数
	 */
	@Column(name = "monthAwardTime", nullable = false)
	@IndexProp(id = 5)
	protected long monthAwardTime = 0;
	
	/**
	 * 免费体验结束时间
	 */
	@Column(name = "freeEndTime", nullable = false)
	@IndexProp(id = 6)
	protected long freeEndTime = 0;
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 9)
	protected boolean invalid;

	/**
	 * 补卡时设置为1，过期时检测到为1则自动续费
	 */
	@Column(name = "ready", nullable = false)
	@IndexProp(id = 10)
	protected int ready = 0;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public boolean isCommonUnlock() {
		return commonUnlockTime > 0;
	}
	
	public long getCommonUnlockTime() {
		return commonUnlockTime;
	}

	public void setCommonUnlockTime(long commonUnlockTime) {
		this.commonUnlockTime = commonUnlockTime;
	}

	public long getAdvancedEndTime() {
		return advancedEndTime;
	}

	public void setAdvancedEndTime(long advancedEndTime) {
		this.advancedEndTime = advancedEndTime;
	}

	public long getFreeEndTime() {
		return freeEndTime;
	}

	public void setFreeEndTime(long freeEndTime) {
		this.freeEndTime = freeEndTime;
	}

	public long getWeekAwardTime() {
		return weekAwardTime;
	}

	public void setWeekAwardTime(long weekAwardTime) {
		this.weekAwardTime = weekAwardTime;
	}

	public void addWeekAwardTime() {
		this.weekAwardTime++;
		notifyUpdate();
	}
	
	public long getMonthAwardTime() {
		return monthAwardTime;
	}

	public void setMonthAwardTime(long monthAwardTime) {
		this.monthAwardTime = monthAwardTime;
	}
	
	public void addMonthAwardTime() {
		this.monthAwardTime++;
		notifyUpdate();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		playerId = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public int getReady() {
		return ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}
}
