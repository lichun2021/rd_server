package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

/**
 * 码头
 * @author PhilChen
 *
 */
@Entity
@Table(name = "wharf")
public class WharfEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = "";
	
	/** 最后一次刷新奖励时间*/
	@Column(name = "lastRefreshTime", nullable = false)
    @IndexProp(id = 2)
	private long lastRefreshTime;
	
	/** 奖励刷新所需总时间，毫秒*/
	@Column(name = "awardTime", nullable = false)
    @IndexProp(id = 3)
	private int awardTime;

	/** 当前可领取的奖励id，0表示没有奖励*/
	@Column(name = "awardId", nullable = false)
    @IndexProp(id = 4)
	private int awardId;
	
	/** 当前可领取的奖池id*/
	@Column(name = "awardPoolId", nullable = false)
    @IndexProp(id = 5)
	private int awardPoolId;
	
	/** 是否已领取码头奖励*/
	@Column(name = "isTookAward", nullable = false)
    @IndexProp(id = 6)
	private boolean isTookAward;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;
	
	public WharfEntity() {
	}
	
	public WharfEntity(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}
	
	public boolean isTookAward() {
		return isTookAward;
	}
	
	public void setTookAward(boolean isTookAward) {
		this.isTookAward = isTookAward;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public int getAwardTime() {
		return awardTime;
	}
	
	public void setAwardTime(int awardTime) {
		this.awardTime = awardTime;
	}
	
	public int getAwardPoolId() {
		return awardPoolId;
	}
	
	public void setAwardPoolId(int awardPoolId) {
		this.awardPoolId = awardPoolId;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("whart entity primaryKey is playerId");
	}

	
	public String getOwnerKey() {
		return playerId;
	}
}
