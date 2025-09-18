package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "player_ghost_tower")
public class PlayerGhostTowerEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";
	
	@Column(name = "stageId", nullable = false)
	@IndexProp(id = 3)
	private int stageId;
	
	@Column(name = "productTime")
	@IndexProp(id = 4)
	private long productTime;
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 5)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 6)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 7)
	protected boolean invalid;


	public PlayerGhostTowerEntity() {
	}


	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public void setUpdateTime(long updateTime) {
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

	
	public int getStageId() {
		return stageId;
	}


	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public long getProductTime() {
		return productTime;
	}


	public void setProductTime(long productTime) {
		this.productTime = productTime;
	}




	@Override
	public String getOwnerKey() {
		return this.playerId;
	}



}
