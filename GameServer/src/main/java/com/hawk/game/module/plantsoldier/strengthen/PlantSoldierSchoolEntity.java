package com.hawk.game.module.plantsoldier.strengthen;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

@Entity
@Table(name = "plant_soldier_school")
public class PlantSoldierSchoolEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 1)
	private String playerId = "";

	@Column(name = "instrumentSerialized")
	@IndexProp(id = 2)
	private String instrumentSerialized;

	@Column(name = "cracksSerialized")
	@IndexProp(id = 3)
	private String cracksSerialized;

	@Column(name = "crystalSerialized")
	@IndexProp(id = 4)
	private String crystalSerialized;

	@Column(name = "strengthenSerialized")
	@IndexProp(id = 5)
	private String strengthenSerialized;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 16)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 17)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 18)
	protected boolean invalid;

	/**
	 * 军衔强化
	 */
	@Column(name = "militarySerialized")
	@IndexProp(id = 19)
	private String militarySerialized;

	@Column(name = "militarySerialized3")
	@IndexProp(id = 20)
	private String militarySerialized3;

	@Column(name = "switchInfo")
	@IndexProp(id = 21)
	private String switchInfo;
	
	@Transient
	private PlantSoldierSchool plantSchoolObj;

	// 记得创建时要调用一下
	@Override
	public void afterRead() {
		super.afterRead();
		this.plantSchoolObj = PlantSoldierSchool.create(this);
	}

	@Override
	public void beforeWrite() {
		if (null != plantSchoolObj) {
			instrumentSerialized = plantSchoolObj.instrumentSerialize();
			cracksSerialized = plantSchoolObj.cracksSerialize();
			crystalSerialized = plantSchoolObj.crystalSerialize();
			strengthenSerialized = plantSchoolObj.strengthenSerialize();
			militarySerialized = plantSchoolObj.militarySerialize();
			militarySerialized3 = plantSchoolObj.militarySerialize3();
			switchInfo = plantSchoolObj.switchSerialize();
		}
		super.beforeWrite();
	}

	public PlantSoldierSchool getPlantSchoolObj() {
		return plantSchoolObj;
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

	public void setHeroObj(PlantSoldierSchool heroObj) {
		this.plantSchoolObj = heroObj;
	}

	public void setPlantSchoolObj(PlantSoldierSchool plantSchoolObj) {
		this.plantSchoolObj = plantSchoolObj;
	}

	public String getInstrumentSerialized() {
		return instrumentSerialized;
	}

	public void setInstrumentSerialized(String instrumentSerialized) {
		this.instrumentSerialized = instrumentSerialized;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("plant_soldier_school entity primaryKey is playerId");
	}

	public String getOwnerKey() {
		return playerId;
	}

	public String getCracksSerialized() {
		return cracksSerialized;
	}

	public void setCracksSerialized(String cracksSerialized) {
		this.cracksSerialized = cracksSerialized;
	}

	public String getCrystalSerialized() {
		return crystalSerialized;
	}

	public void setCrystalSerialized(String crystalSerialized) {
		this.crystalSerialized = crystalSerialized;
	}

	public String getStrengthenSerialized() {
		return strengthenSerialized;
	}

	public void setStrengthenSerialized(String strengthenSerialized) {
		this.strengthenSerialized = strengthenSerialized;
	}

	public String getMilitarySerialized() {
		return militarySerialized;
	}

	public void setMilitarySerialized(String militarySerialized) {
		this.militarySerialized = militarySerialized;
	}

	public String getMilitarySerialized3() {
		return militarySerialized3;
	}

	public void setMilitarySerialized3(String militarySerialized3) {
		this.militarySerialized3 = militarySerialized3;
	}

	public String getSwitchInfo() {
		return switchInfo;
	}

	public void setSwitchInfo(String switchInfo) {
		this.switchInfo = switchInfo;
	}
}
