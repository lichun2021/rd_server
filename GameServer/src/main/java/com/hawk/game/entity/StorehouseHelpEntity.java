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
 * 已帮助宝藏
 * 
 * @author lwt
 *
 */
@Entity
@Table(name = "story_house_help")
public class StorehouseHelpEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "targetId")
    @IndexProp(id = 3)
	private String targetId; // 被帮助人id

	@Column(name = "storehouseId", nullable = false)
    @IndexProp(id = 4)
	private String storehouseId; // 被帮助人宝藏id(uuid )

	@Column(name = "storeId", nullable = false)
    @IndexProp(id = 5)
	private int storeId; //

	@Column(name = "openTime", nullable = false)
    @IndexProp(id = 6)
	protected long openTime = 0; // 开启时间 (开启得到帮助奖励)

	@Column(name = "collect")
    @IndexProp(id = 7)
	protected boolean collect; // 已收取

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 8)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 9)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 10)
	protected boolean invalid;

	public StorehouseHelpEntity() {

	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public String getTargetId() {
		return targetId;
	}

	public boolean isCollect() {
		return collect;
	}

	public void setCollect(boolean collect) {
		this.collect = collect;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public String getStorehouseId() {
		return storehouseId;
	}

	public void setStorehouseId(String storehouseId) {
		this.storehouseId = storehouseId;
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
		this.id = primaryKey;

	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
