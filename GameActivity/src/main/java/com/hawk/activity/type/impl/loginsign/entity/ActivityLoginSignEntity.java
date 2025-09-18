package com.hawk.activity.type.impl.loginsign.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;

/**
 * 登录签到活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_login_sign")
public class ActivityLoginSignEntity extends HawkDBEntity implements IActivityDataEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/** 最后一次领取奖励时间 */
    @IndexProp(id = 4)
	@Column(name = "lastTookTime", nullable = false)
	private long lastTookTime;
	
	/** 是否已经领取奖励 */
    @IndexProp(id = 5)
	@Column(name = "tookItemId", nullable = false)
	private int tookItemId;

    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	public ActivityLoginSignEntity() {
	}
	
	public ActivityLoginSignEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public ActivityLoginSignEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.tookItemId = 0;
		this.termId = termId;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public long getLastTookTime() {
		return lastTookTime;
	}

	public void setLastTookTime(long lastTookTime) {
		this.lastTookTime = lastTookTime;
	}

	public int getTookItemId() {
		return tookItemId;
	}
	
	public void setTookItemId(int tookItemId) {
		this.tookItemId = tookItemId;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
