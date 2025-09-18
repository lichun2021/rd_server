package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 * 玩家状态实体对象
 *
 * @author david
 */
@Entity
@Table(name = "status_data")
public class StatusDataEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
	@Column(name = "uuid", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String uuid;

	// 玩家ID
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;

	// 状态ID
	@Column(name = "statusId", nullable = false)
    @IndexProp(id = 3)
	private int statusId;

	// 状态值
	@Column(name = "val", nullable = false)
    @IndexProp(id = 4)
	private int val;

	// 状态类型
	@Column(name = "type", nullable = false)
    @IndexProp(id = 5)
	private int type;
	
	// 作用对象
	@Column(name = "targetId")
    @IndexProp(id = 6)
	private String targetId;

	@Column(name = "startTime", nullable = false)
    @IndexProp(id = 7)
	private long startTime;

	@Column(name = "endTime", nullable = false)
    @IndexProp(id = 8)
	private long endTime;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 9)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 10)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 11)
	protected boolean invalid;

	@Transient
	private boolean pushed = false;
	
	@Transient
	private boolean shieldNoticed = false;
	
	// 主动破罩
	@Transient
	private boolean initiative = false;

	public StatusDataEntity() {
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean getPushed() {
		return pushed;
	}

	public void resetPushed(boolean pushed) {
		this.pushed = pushed;
	}
	
	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public boolean isShieldNoticed() {
		return shieldNoticed;
	}

	public void resetShieldNoticed(boolean shieldNoticed) {
		this.shieldNoticed = shieldNoticed;
	}
	
	public boolean isInitiative() {
		return initiative;
	}

	public void setInitiative(boolean initiative) {
		this.initiative = initiative;
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
		return uuid;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		uuid = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
