package com.hawk.game.module.plantsoldier.science;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "plant_science")
public class PlantScienceEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "plantScienceSerialized", nullable = false)
	@IndexProp(id = 3)
	private String plantScienceSerialized;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 4)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 5)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 6)
	protected boolean invalid;

	@Transient
	private PlantScience scienceObj;

	public PlantScienceEntity() {
	}

	@Override
	public void beforeWrite() {
		if (null != scienceObj) {
			plantScienceSerialized = scienceObj.serializScienceComponent();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		PlantScience.create(this);
		super.afterRead();
	}

	public void recordScienceObj(PlantScience PlantScience) {
		this.scienceObj = PlantScience;
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
	
	

	public String getPlantScienceSerialized() {
		return plantScienceSerialized;
	}

	public void setPlantScienceSerialized(String plantScienceSerialized) {
		this.plantScienceSerialized = plantScienceSerialized;
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

	public PlantScience getSciencObj() {
		if (!scienceObj.isEfvalLoad()) {
			scienceObj.loadEffVal();
		}
		return scienceObj;
	}

	

}
