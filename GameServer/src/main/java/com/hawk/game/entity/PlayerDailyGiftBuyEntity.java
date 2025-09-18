package com.hawk.game.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 玩家基础数据
 *
 * @author hawk
 *
 */
@Entity
@Table(name = "player_daily_gift_buy")
public class PlayerDailyGiftBuyEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = null;

		
    @IndexProp(id = 2)
	@Column(name = "termId", nullable = false)
	private int termId;
    
    @IndexProp(id = 3)
    @Column(name = "itemRecord", nullable = false)
    private String itemRecord;
    
    @IndexProp(id = 4)
	@Column(name = "refreshTime", nullable = false)
    private long refreshTime;
    
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 7)
	private long updateTime;

	// 记录是否有效
	@Column(name = "invalid")
    @IndexProp(id = 8)
	private boolean invalid;

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private Map<Integer, Integer> itemRecordMap = new ConcurrentHashMap<>();
	
	
	public PlayerDailyGiftBuyEntity() {
	}
	

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}


	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public long getRefreshTime() {
		return refreshTime;
	}
	
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public Map<Integer, Integer> getItemRecordMap() {
		return itemRecordMap;
	}
	
	public void setItemRecordMap(Map<Integer, Integer> itemRecordMap) {
		this.itemRecordMap = itemRecordMap;
	}
	

	public void addItemRecordCount(int itemId,int itemNum){
		int count = this.itemRecordMap.getOrDefault(itemId, 0) + itemNum;
		this.itemRecordMap.put(itemId, count);
		this.notifyUpdate();
	}
	
	public int getItemRecordCount(){
		int sum = 0;
		for(int count : this.itemRecordMap.values()){
			sum += count;
		}
		return sum;
	}
	
	public void clearItemRecordCount(){
		this.itemRecordMap = new ConcurrentHashMap<Integer, Integer>();
		this.notifyUpdate();
	}
	
	
	@Override
    public void beforeWrite() {
    	this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    	this.itemRecord = SerializeHelper.mapToString(itemRecordMap);
    }
    
    @Override
    public void afterRead() {
    	this.itemList.clear();
    	SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
    	this.itemRecordMap = SerializeHelper.stringToMap(this.itemRecord, Integer.class, Integer.class);
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
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("pay state entity primaryKey is playerId");		
	}

	
	public String getOwnerKey() {
		return playerId;
	}
}
