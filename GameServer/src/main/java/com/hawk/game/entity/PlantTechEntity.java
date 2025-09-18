package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.module.plantfactory.tech.PlantTech;

@Entity
@Table(name = "plant_tech")
public class PlantTechEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "cfgId", nullable = false)
	@IndexProp(id = 3)
	private int cfgId;

	@Column(name = "buildType", nullable = false)
	@IndexProp(id = 4)
	private int buildType;

	@Column(name = "chipSerialized", nullable = false)
	@IndexProp(id = 11)
	private String chipSerialized;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 16)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 17)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 18)
	protected boolean invalid;

	@Transient
	private PlantTech techObj;

	public PlantTechEntity() {
	}

	@Override
	public void beforeWrite() {
		if (null != techObj) {
			chipSerialized = techObj.serializChips();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		PlantTech.create(this);
		super.afterRead();
	}

	public void recordTechObj(PlantTech heroObj) {
		this.techObj = heroObj;
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
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;

	}
	
	@Override
	public String getOwnerKey() {
		return playerId;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public String getChipSerialized() {
		return chipSerialized;
	}

	public void setChipSerialized(String chipSerialized) {
		this.chipSerialized = chipSerialized;
	}

	public PlantTech getTechObj() {
		if (!techObj.isEfvalLoad()) {
			techObj.loadEffVal();
		}
		return techObj;
	}

	public void setTechObj(PlantTech techObj) {
		this.techObj = techObj;
	}

	public int getBuildType() {
		return buildType;
	}

	public void setBuildType(int buildType) {
		this.buildType = buildType;
	}

}
