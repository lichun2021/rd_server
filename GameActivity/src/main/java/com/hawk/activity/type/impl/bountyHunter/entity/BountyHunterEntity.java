package com.hawk.activity.type.impl.bountyHunter.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;

@Entity
@Table(name = "activity_bounty_hunter")
public class BountyHunterEntity extends HawkDBEntity implements IActivityDataEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;

    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;

    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;

    @IndexProp(id = 4)
	@Column(name = "pool", nullable = false)
	private int pool;
	
    @IndexProp(id = 5)
	@Column(name = "freeItemDay", nullable = false)
	private int freeItemDay;
	

    @IndexProp(id = 6)
	@Column(name = "hitType", nullable = false)
	private int hitType;

    @IndexProp(id = 7)
	@Column(name = "bossHp", nullable = false)
	private int bossHp; // boss血量 A boss 10 -0

    @IndexProp(id = 8)
	@Column(name = "lefState", nullable = false)
	private int lefState; // 0 1 2 存活 死亡 逃跑

    @IndexProp(id = 9)
	@Column(name = "bossBHit", nullable = false)
	private int bossBHit; // bossB 受击数

    @IndexProp(id = 10)
	@Column(name = "poolARount", nullable = false)
	private int poolARount; // 连续A池循环

    @IndexProp(id = 11)
	@Column(name = "bossBNotRun", nullable = false)
	private int bossBNotRun; // B池不会逃跑

    @IndexProp(id = 12)
	@Column(name = "bossBNotDie", nullable = false)
	private int bossBNotDie; // B池不死

    @IndexProp(id = 13)
	@Column(name = "costMutil", nullable = false)
	private int costMutil;

    @IndexProp(id = 14)
	@Column(name = "rewardMutil", nullable = false)
	private int rewardMutil;
	// 倍数剩余次数 , 归零后重新随机hit表
    @IndexProp(id = 15)
	@Column(name = "mutilCount", nullable = false)
	private int mutilCount;

    @IndexProp(id = 16)
	@Column(name = "batter", nullable = false)
	private int batter;// 连击数

    @IndexProp(id = 17)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 18)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 19)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	public BountyHunterEntity() {
	}

	public BountyHunterEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
	}

	@Override
	public void afterRead() {
	}

	public int getPool() {
		return pool;
	}

	public void setPool(int pool) {
		this.pool = pool;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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

	public int getCostMutil() {
		return costMutil;
	}

	public void setCostMutil(int costMutil) {
		this.costMutil = costMutil;
	}

	public int getRewardMutil() {
		return rewardMutil;
	}

	public void setRewardMutil(int rewardMutil) {
		this.rewardMutil = rewardMutil;
	}

	public int getMutilCount() {
		return mutilCount;
	}

	public void setMutilCount(int mutilCount) {
		this.mutilCount = mutilCount;
	}

	public int getBossHp() {
		return bossHp;
	}

	public void setBossHp(int bossHp) {
		this.bossHp = bossHp;
	}

	public int getPoolARount() {
		return poolARount;
	}

	public void setPoolARount(int poolARount) {
		this.poolARount = poolARount;
	}

	public int getBossBNotRun() {
		return bossBNotRun;
	}

	public void setBossBNotRun(int bossBNotRun) {
		this.bossBNotRun = bossBNotRun;
	}

	public int getBossBNotDie() {
		return bossBNotDie;
	}

	public void setBossBNotDie(int bossBNotDie) {
		this.bossBNotDie = bossBNotDie;
	}

	public int getBossBHit() {
		return bossBHit;
	}

	public void setBossBHit(int bossBHit) {
		this.bossBHit = bossBHit;
	}

	public int getHitType() {
		return hitType;
	}

	public void setHitType(int hitType) {
		this.hitType = hitType;
	}

	public int getBatter() {
		return batter;
	}

	public void setBatter(int batter) {
		this.batter = batter;
	}

	public int getLefState() {
		return lefState;
	}

	public void setLefState(int lefState) {
		this.lefState = lefState;
	}

	public int getFreeItemDay() {
		return freeItemDay;
	}

	public void setFreeItemDay(int freeItemDay) {
		this.freeItemDay = freeItemDay;
	}

}
