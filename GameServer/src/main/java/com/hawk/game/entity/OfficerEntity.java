package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

/**
 * 官职实体对象
 *
 * @author
 */
@Entity
@Table(name = "officer")
public class OfficerEntity extends HawkDBEntity {
	@Id
	@Column(name = "officerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private int officerId = 0;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	// 冷却到期时间
	@Column(name = "endTime")
    @IndexProp(id = 3)
	private long endTime;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 4)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 5)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 6)
	protected boolean invalid;

	public OfficerEntity() {
	}

	public int getOfficerId() {
		return officerId;
	}

	public void setOfficerId(int id) {
		this.officerId = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
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

	@Override
	public String getPrimaryKey() {
		return officerId+"";
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("officer entity primaryKey is playerId");
		
	}	
}
