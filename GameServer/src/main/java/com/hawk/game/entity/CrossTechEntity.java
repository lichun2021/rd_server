package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.GsConfig;

/**
 * 远征科技实体
 *
 * @author shadow
 *
 */
@Entity
@Table(name = "cross_tech")
public class CrossTechEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = null;
	
	@Column(name = "techId", nullable = false)
    @IndexProp(id = 3)
	private int techId = 0;

	@Column(name = "level", nullable = false)
    @IndexProp(id = 4)
	private int level = 0;

	@Column(name = "researching", nullable = false)
    @IndexProp(id = 5)
	private boolean researching = false;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;

	public CrossTechEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public int getCfgId() {
		return techId * 100 + level;
	}

	public int getNextLevelCfgId(){
		return techId * 100 + level+1;
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

	public int getTechId() {
		return techId;
	}

	public void setTechId(int techId) {
		this.techId = techId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isResearching() {
		return this.researching;
	}

	public void setResearching(boolean researching) {
		this.researching = researching;
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
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
