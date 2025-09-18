package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "gacha")
public class PlayerGachaEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;

	@Column(name = "dayOfYear")
    @IndexProp(id = 3)
	private int dayOfYear; // 如果不是今天,要重置freeTimesUsed

	@Column(name = "count")
    @IndexProp(id = 4)
	private int count; // 累计抽取总次数

	@Column(name = "gachaType")
    @IndexProp(id = 5)
	private int gachaType;

	@Column(name = "freeTimesUsed")
    @IndexProp(id = 6)
	private int freeTimesUsed; // 已抽免费数
	
	@Column(name = "firstGachaUsed")
    @IndexProp(id = 7)
	private int firstGachaUsed;

	@Column(name = "nextFree")
    @IndexProp(id = 8)
	private long nextFree; // 下次免费时间

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 9)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 10)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 11)
	private boolean invalid;

	@Column(name = "dayCount")
    @IndexProp(id = 12)
	private int dayCount; // 当日已经抽取次数
	
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

	public int getGachaType() {
		return gachaType;
	}

	public void setGachaType(int gachaType) {
		this.gachaType = gachaType;
	}

	public int getFreeTimesUsed() {
		return freeTimesUsed;
	}

	public void setFreeTimesUsed(int freeTimesUsed) {
		this.freeTimesUsed = freeTimesUsed;
	}

	public long getNextFree() {
		return nextFree;
	}

	public void setNextFree(long nextFree) {
		this.nextFree = nextFree;
	}

	public int getDayOfYear() {
		return dayOfYear;
	}

	public void setDayOfYear(int dayOfYear) {
		this.dayOfYear = dayOfYear;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
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

	public int getFirstGachaUsed() {
		return firstGachaUsed;
	}

	public void setFirstGachaUsed(int firstGachaUsed) {
		this.firstGachaUsed = firstGachaUsed;
	}

	public int getDayCount() {
		return dayCount;
	}

	public void setDayCount(int dayCount) {
		this.dayCount = dayCount;
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
