package com.hawk.activity.type.impl.starInvest.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.starInvest.StarInvestExploreCell;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_start_invest")
public class StarInvestEntity extends ActivityDataEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**购买数据 */
    @IndexProp(id = 4)
	@Column(name = "buyInfo", nullable = false)
	private String buyInfo;
    
    /**购买数据 */
    @IndexProp(id = 5)
	@Column(name = "freeInfo", nullable = false)
	private String freeInfo;
	
	/**日常任务数据 */
    @IndexProp(id = 6)
	@Column(name = "daliyTask", nullable = false)
	private String daliyTask;
    
    /** 活动成就项数据 */
	@IndexProp(id = 7)
	@Column(name = "cells", nullable = false)
	private String cells;
	
	@IndexProp(id = 8)
	@Column(name = "rechargeCount", nullable = false)
	private int rechargeCount;
    
    /** 活动成就项数据 */
	@IndexProp(id = 9)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	@IndexProp(id = 10)
	@Column(name = "speedItemBuyCount", nullable = false)
	private int speedItemBuyCount;
	
    
	@IndexProp(id = 11)
	@Column(name = "initTime", nullable = false)
	private long initTime;
	
    @IndexProp(id = 12)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
	
    @IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private Map<Integer, Integer> daliyTaskMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Integer> buyInfoMap = new ConcurrentHashMap<>();
	
	@Transient
	private Map<Integer, Long> freeInfoMap = new ConcurrentHashMap<>();
	

	@Transient
	private Map<Integer, StarInvestExploreCell> cellMap = new ConcurrentHashMap<>();
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public StarInvestEntity() {
	}
	
	
	public StarInvestEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		
		buyInfo = "";
		freeInfo = "";
		daliyTask = "";
		cells = "";
		achieveItems = "";
		loginDays = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList);
		this.daliyTask = SerializeHelper.mapToString(daliyTaskMap);
		this.buyInfo = SerializeHelper.mapToString(buyInfoMap);
		this.freeInfo = SerializeHelper.mapToString(freeInfoMap);
		this.cells = SerializeHelper.mapToString(cellMap);
	}
	
	@Override
	public void afterRead() {
		this.daliyTaskMap = SerializeHelper.stringToMap(daliyTask);
		this.buyInfoMap = SerializeHelper.stringToMap(buyInfo);
		this.freeInfoMap = SerializeHelper.stringToMap(this.freeInfo, Integer.class, Long.class);
		SerializeHelper.stringToMap(this.cells, Integer.class, StarInvestExploreCell.class,this.cellMap);
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
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


	public String getDaliyTask() {
		return daliyTask;
	}

	public void setDaliyTask(String daliyTask) {
		this.daliyTask = daliyTask;
	}


	public String getBuyInfo() {
		return buyInfo;
	}

	public void setBuyInfo(String buyInfo) {
		this.buyInfo = buyInfo;
	}

	public Map<Integer, Integer> getDaliyTaskMap() {
		return daliyTaskMap;
	}

	public void setDaliyTaskMap(Map<Integer, Integer> daliyTaskMap) {
		this.daliyTaskMap = daliyTaskMap;
	}

	public Map<Integer, Integer> getBuyInfoMap() {
		return buyInfoMap;
	}

	public void setBuyInfoMap(Map<Integer, Integer> buyInfoMap) {
		this.buyInfoMap = buyInfoMap;
	}

	
	public Map<Integer, Long> getFreeInfoMap() {
		return freeInfoMap;
	}
	
	
	public void setFreeGiftRewardTime(int id,long time){
		this.freeInfoMap.put(id, time);
	}
	
	
	public Map<Integer, StarInvestExploreCell> getCellMap() {
		return cellMap;
	}
	
	
	public int getRechargeCount() {
		return rechargeCount;
	}
	
	public void setRechargeCount(int rechargeCount) {
		this.rechargeCount = rechargeCount;
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public int getSpeedItemBuyCount() {
		return speedItemBuyCount;
	}
	
	public void setSpeedItemBuyCount(int speedItemBuyCount) {
		this.speedItemBuyCount = speedItemBuyCount;
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

	@Override
	public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }
    
    @Override
	public String getLoginDaysStr() {
		return this.loginDays;
	}

	
}
