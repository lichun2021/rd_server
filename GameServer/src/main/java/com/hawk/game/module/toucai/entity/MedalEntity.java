package com.hawk.game.module.toucai.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

@Entity
@Table(name = "medal_factory")
public class MedalEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId;

	@Column(name = "exp", nullable = false)
	@IndexProp(id = 2)
	private int exp;

	// 菜地
	@Column(name = "collectStr", nullable = false)
	@IndexProp(id = 3)
	private String collectStr = "";

	// 正在偷取的菜地playerId
	@Column(name = "stealStr", nullable = false)
	@IndexProp(id = 4)
	private String stealStr = "";

	// 今日已偷取
	@Column(name = "stealTodayStr", nullable = false)
	@IndexProp(id = 5)
	private String stealTodayStr = "";

	@Column(name = "canSteal", nullable = false)
	@IndexProp(id = 6)
	private int canSteal;

	@Column(name = "enemyStr", nullable = false)
	@IndexProp(id = 7)
	private String enemyStr = "";

	@Column(name = "refreshStr", nullable = false)
	@IndexProp(id = 8)
	private String refreshStr = "";

	@Column(name = "dailyReward")
	@IndexProp(id = 9)
	protected int dailyReward;

	@Column(name = "dailyRefresh")
	@IndexProp(id = 10)
	protected int dailyRefresh; // 今日可刷新数

	@Column(name = "refreshCool")
	@IndexProp(id = 11)
	protected long refreshCool; // 刷新冷却
	
	@Column(name = "lastRefreshDay")
	@IndexProp(id = 12)
	protected int lastRefreshDay; 
	
	@Column(name = "leyuzhuren")
	@IndexProp(id = 13)
	protected int leyuzhuren; 

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
	private MedalFactoryObj factory;

	public MedalEntity() {
	}

	@Override
	public void beforeWrite() {
		if (null != factory) {
			// this.skillSerialized = heroObj.serializSkill();
			this.collectStr = factory.serializCollect();
			this.stealStr = factory.serializSteal();
			this.enemyStr = factory.serializEnemys();
			this.refreshStr = factory.serializRefresh();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		MedalFactoryObj.create(this);
		super.afterRead();
	}

	public MedalFactoryObj getFactoryObj() {
		if (factory == null) {
			MedalFactoryObj.create(this);
		}
		return factory;
	}

	public void recordFactoryObj(MedalFactoryObj obj) {
		this.factory = obj;
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
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.playerId = primaryKey;

	}

	public String getOwnerKey() {
		return playerId;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public String getCollectStr() {
		return collectStr;
	}

	public void setCollectStr(String collectStr) {
		this.collectStr = collectStr;
	}

	public String getStealStr() {
		return stealStr;
	}

	public void setStealStr(String stealStr) {
		this.stealStr = stealStr;
	}

	public String getStealTodayStr() {
		return stealTodayStr;
	}

	public void setStealTodayStr(String stealTodayStr) {
		this.stealTodayStr = stealTodayStr;
	}

	public int getCanSteal() {
		return canSteal;
	}

	public void setCanSteal(int canSteal) {
		this.canSteal = canSteal;
	}

	public String getEnemyStr() {
		return enemyStr;
	}

	public void setEnemyStr(String enemyStr) {
		this.enemyStr = enemyStr;
	}

	public String getRefreshStr() {
		return refreshStr;
	}

	public void setRefreshStr(String refreshStr) {
		this.refreshStr = refreshStr;
	}

	public int getDailyReward() {
		return dailyReward;
	}

	public void setDailyReward(int dailyReward) {
		this.dailyReward = dailyReward;
	}

	public int getDailyRefresh() {
		return dailyRefresh;
	}

	public void setDailyRefresh(int dailyRefresh) {
		this.dailyRefresh = dailyRefresh;
	}

	public long getRefreshCool() {
		return refreshCool;
	}

	public void setRefreshCool(long refreshCool) {
		this.refreshCool = refreshCool;
	}

	public int getLastRefreshDay() {
		return lastRefreshDay;
	}

	public void setLastRefreshDay(int lastRefreshDay) {
		this.lastRefreshDay = lastRefreshDay;
	}

	public int getLeyuzhuren() {
		return leyuzhuren;
	}

	public void setLeyuzhuren(int leyuzhuren) {
		this.leyuzhuren = leyuzhuren;
	}

}
