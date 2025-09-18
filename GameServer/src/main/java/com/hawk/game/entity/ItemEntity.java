package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 * 物品实体对象
 *
 * @author hawk
 */
@Entity
@Table(name = "item")
public class ItemEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "itemId", nullable = false)
    @IndexProp(id = 3)
	private int itemId = 0;

	@Column(name = "itemCount")
    @IndexProp(id = 4)
	private int itemCount = 0;

	@Column(name = "isNew", nullable = false)
    @IndexProp(id = 5)
	private boolean isNew = true;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	private boolean invalid;

	public ItemEntity() {
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

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
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
