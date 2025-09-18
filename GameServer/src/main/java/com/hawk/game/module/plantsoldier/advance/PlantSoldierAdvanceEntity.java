package com.hawk.game.module.plantsoldier.advance;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

@Entity
@Table(name = "plant_soldier_advance")
public class PlantSoldierAdvanceEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 1)
	private String playerId = "";

	// 用于晋升的兵
	@Column(name = "advanceArmy", nullable = false)
	@IndexProp(id = 3)
	private int advanceArmy;
	// 收取会得到的兵
	@Column(name = "collectArmy", nullable = false)
	@IndexProp(id = 4)
	private int collectArmy;

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

	@Column(name = "advanceTotal")
	@IndexProp(id = 15)
	private int advanceTotal = 0;

	@Column(name = "resTotal")
	@IndexProp(id = 16)
	private String resTotal = "";
	
	@Column(name = "advanceStart")
	@IndexProp(id = 17)
	private long advanceStart = 0;
	
	@Column(name = "advanceEnd")
	@IndexProp(id = 18)
	private long advanceEnd = 0;

	public PlantSoldierAdvanceEntity() {
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getAdvanceArmy() {
		return advanceArmy;
	}

	public void setAdvanceArmy(int advanceArmy) {
		this.advanceArmy = advanceArmy;
	}

	public int getCollectArmy() {
		return collectArmy;
	}

	public void setCollectArmy(int collectArmy) {
		this.collectArmy = collectArmy;
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

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String paramString) {

	}

	public int getAdvanceTotal() {
		return advanceTotal;
	}

	public void setAdvanceTotal(int advanceTotal) {
		this.advanceTotal = advanceTotal;
	}

	public String getResTotal() {
		return resTotal;
	}

	public void setResTotal(String resTotal) {
		this.resTotal = resTotal;
	}

	public long getAdvanceStart() {
		return advanceStart;
	}

	public void setAdvanceStart(long advanceStart) {
		this.advanceStart = advanceStart;
	}

	public long getAdvanceEnd() {
		return advanceEnd;
	}

	public void setAdvanceEnd(long advanceEnd) {
		this.advanceEnd = advanceEnd;
	}

}
