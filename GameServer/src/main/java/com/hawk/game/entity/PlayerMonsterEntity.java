package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 玩家野怪数据实体
 * 
 */
@Entity
@Table(name = "monster")
public class PlayerMonsterEntity  extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	//当前杀死野怪的最大等级
	@Column(name = "maxLevel", nullable = false)
    @IndexProp(id = 3)
	private int maxLevel;
	
	//杀死当前最大等级怪物的数量
	@Column(name = "currentLevelCount", nullable = false)
    @IndexProp(id = 4)
	private int currentLevelCount;
	
	//当前杀死新版野怪的最大等级
	@Column(name = "newMonsterKileLvl", nullable = false)
    @IndexProp(id = 5)
	private int newMonsterKileLvl;
	
	//攻击新版野怪次数
	@Column(name = "attackNewMonsterTimes", nullable = false, columnDefinition = "0")
    @IndexProp(id = 6)
	private int attackNewMonsterTimes;
	
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 7)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 8)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 9)
	protected boolean invalid;
	
	
	@Column(name = "bosskillInfo")
    @IndexProp(id = 10)
	protected String bosskillInfo = "";
	
	
	@Column(name = "bosskillRefreshDay")
    @IndexProp(id = 11)
	protected int bosskillRefreshDay;
	
	
	@Column(name = "dropLimitInfo")
    @IndexProp(id = 12)
	protected String dropLimitInfo;
	
	
	@Column(name = "dropLimitRefreshDay")
    @IndexProp(id = 13)
	protected int dropLimitRefreshDay;;
	
	/**
	 * BOSS击杀数量
	 */
	@Transient
	private Map<Integer, Integer> bosskillMap = new ConcurrentHashMap<>();
	
	
	@Transient
	private Map<Integer, Integer> dropLimitRecordMap = new ConcurrentHashMap<>();
	
	
	
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

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public int getNewMonsterKileLvl() {
		return newMonsterKileLvl;
	}
	
	public int getAttackNewMonsterTimes() {
		return attackNewMonsterTimes;
	}

	public void addAttackNewMonsterTimes() {
		this.attackNewMonsterTimes += 1;
		notifyUpdate();
	}

	public void setNewMonsterKileLvl(int newMonsterKileLvl) {
		this.newMonsterKileLvl = newMonsterKileLvl;
	}

	public int getCurrentLevelCount() {
		return currentLevelCount;
	}

	public void setCurrentLevelCount(int currentLevelCount) {
		this.currentLevelCount = currentLevelCount;
	}

	public void addBossKill(int bossId,int count){
		int yearDay = HawkTime.getYearDay();
		if(this.bosskillRefreshDay != yearDay){
			this.bosskillMap = new ConcurrentHashMap<>();
			this.bosskillRefreshDay = yearDay;
		}
		int bef = this.bosskillMap.getOrDefault(bossId, 0);
		int aft = bef + count;
		this.bosskillMap.put(bossId, aft);
		this.notifyUpdate();
	}
	
	public int getBossKillCountDaily(List<Integer> bossIds){
		if(Objects.isNull(bossIds)){
			return 0;
		}
		int yearDay = HawkTime.getYearDay();
		if(this.bosskillRefreshDay != yearDay){
			return 0;
		}
		int count = 0;
		for(int bid : bossIds){
			count += this.bosskillMap.getOrDefault(bid, 0);
		}
		return count;
	}
	
	public Map<Integer, Integer> getBosskillMap() {
		int yearDay = HawkTime.getYearDay();
		if(this.bosskillRefreshDay != yearDay){
			return new ConcurrentHashMap<>();
		}
		return bosskillMap;
	}
	
	
	public void addDropLimit(int itemId,int count){
		int yearDay = HawkTime.getYearDay();
		if(this.dropLimitRefreshDay != yearDay){
			this.dropLimitRecordMap = new ConcurrentHashMap<>();
			this.dropLimitRefreshDay = yearDay;
		}
		int bef = this.dropLimitRecordMap.getOrDefault(itemId, 0);
		int aft = bef + count;
		this.dropLimitRecordMap.put(itemId, aft);
		this.notifyUpdate();
	}
	
	
	public Map<Integer, Integer> getDropLimitMap() {
		int yearDay = HawkTime.getYearDay();
		if(this.dropLimitRefreshDay != yearDay){
			return new ConcurrentHashMap<>();
		}
		return this.dropLimitRecordMap;
	}
	
	
	@Override
	public void beforeWrite() {
		this.bosskillInfo = SerializeHelper.mapToString(bosskillMap);
		this.dropLimitInfo = SerializeHelper.mapToString(this.dropLimitRecordMap);
	}
	
	
	@Override
	public void afterRead() {
		this.bosskillMap = SerializeHelper.stringToMap(bosskillInfo, Integer.class, Integer.class);
		this.dropLimitRecordMap = SerializeHelper.stringToMap(dropLimitInfo, Integer.class, Integer.class);
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
	
	public String getOwnerKey() {
		return playerId;
	}
}
