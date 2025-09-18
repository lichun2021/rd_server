package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

import com.hawk.game.GsConfig;

/**
 * 国家建设处Entity
 * @author zhenyu.shang
 * @since 2022年3月22日
 */
@Entity
@Table(name = "nation_construction")
public class NationConstructionEntity extends HawkDBEntity {

	@Id
	@Column(name = "buildingId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private int buildingId;
	
	@Column(name = "level")
    @IndexProp(id = 2)
	private int level;
	
	@Column(name = "buildingStatus")
    @IndexProp(id = 3)
	private int buildingStatus;
	
	@Column(name = "buildVal")
    @IndexProp(id = 4)
	private int buildVal;
	
	@Column(name = "totalVal")
    @IndexProp(id = 5)
	private int totalVal;
	
	@Column(name = "buildTime")
    @IndexProp(id = 6)
	private long buildTime;
	
	@Column(name = "createTime")
    @IndexProp(id = 7)
	private long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	private boolean invalid;
	
	
	public NationConstructionEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public int getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBuildVal() {
		return buildVal;
	}

	public void setBuildVal(int buildVal) {
		this.buildVal = buildVal;
	}

	public int getTotalVal() {
		return totalVal;
	}

	public void setTotalVal(int totalVal) {
		this.totalVal = totalVal;
	}

	public long getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
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

	public int getBuildingStatus() {
		return buildingStatus;
	}

	public void setBuildingStatus(int buildingStatus) {
		this.buildingStatus = buildingStatus;
	}

	@Override
	public String getPrimaryKey() {
		return String.valueOf(this.buildingId);
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
	
}
