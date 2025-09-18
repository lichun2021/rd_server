package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

/**
 * 自定义数据实体
 *
 * @author hawk
 */
@Entity
@Table(name = "custom_data")
public class CustomDataEntity extends HawkDBEntity {
	@Id
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "type", nullable = false)
    @IndexProp(id = 3)
	private String type = "";

	@Column(name = "value")
    @IndexProp(id = 4)
	private int value = 0;

	@Column(name = "arg")
    @IndexProp(id = 5)
	private String arg = "";

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	private boolean invalid;

	public CustomDataEntity() {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
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
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
