package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

@Entity
@Table(name = "plant_factory")
public class PlantFactoryEntity extends HawkDBEntity {
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "plantCfgId", nullable = false)
	@IndexProp(id = 3)
	private int plantCfgId;

	@Column(name = "factoryType", nullable = false)
	@IndexProp(id = 4)
	private int factoryType;

	// 最后一次资源收取时间(初始时间为建筑新健时间) 已扩展至城内所有资源建筑
	@Column(name = "lastResStoreTime")
	@IndexProp(id = 5)
	private long lastResStoreTime = 0;

	@Column(name = "resStore")
	@IndexProp(id = 6)
	private double resStore = 0;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 12)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 13)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 14)
	protected boolean invalid;

	public PlantFactoryEntity() {
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

	public int getPlantCfgId() {
		return plantCfgId;
	}

	public void setPlantCfgId(int plantCfgId) {
		this.plantCfgId = plantCfgId;
	}

	public int getFactoryType() {
		return factoryType;
	}

	public void setFactoryType(int factoryType) {
		this.factoryType = factoryType;
	}

	public long getLastResStoreTime() {
		return lastResStoreTime;
	}

	public void setLastResStoreTime(long lastResStoreTime) {
		this.lastResStoreTime = lastResStoreTime;
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

	@Override
	public String getOwnerKey() {
		return playerId;
	}

	public double getResStore() {
		return resStore;
	}

	public int getResStoreIntVal() {
		return (int) (resStore * 10000);
	}

	public void setResStore(double resStore) {
		this.resStore = resStore;
	}

}
