package com.hawk.activity.type.impl.drogenBoatFestival.recharge.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_dragon_boat_recharge")
public class DragonBoatRechargeEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "rechargeDays", nullable = false)
	private int rechargeDays;
	
	
    @IndexProp(id = 5)
	@Column(name = "lastRechargeTime", nullable = false)
	private long lastRechargeTime;
	
	
	/** 活动成就项数据,不需要每日刷新 */
    @IndexProp(id = 6)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/** 活动成就项数据,每日刷新 */
    @IndexProp(id = 7)
	@Column(name = "achieveItemsDay", nullable = false)
	private String achieveItemsDay;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();

	public DragonBoatRechargeEntity() {
	}
	
	
	
	public DragonBoatRechargeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.achieveItemsDay = "";
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
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	
	
	

	public String getAchieveItemsDay() {
		return achieveItemsDay;
	}



	public void setAchieveItemsDay(String achieveItemsDay) {
		this.achieveItemsDay = achieveItemsDay;
	}

	


	public int getRechargeDays() {
		return rechargeDays;
	}



	public void setRechargeDays(int rechargeDays) {
		this.rechargeDays = rechargeDays;
	}



	public long getLastRechargeTime() {
		return lastRechargeTime;
	}



	public void setLastRechargeTime(long lastRechargeTime) {
		this.lastRechargeTime = lastRechargeTime;
	}



	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	
	

	public List<AchieveItem> getItemListDay() {
		return itemListDay;
	}



	public void resetItemListDay(List<AchieveItem> itemListDay) {
		this.itemListDay = itemListDay;
		this.notifyUpdate();
	}

	public List<AchieveItem> getAchieveList(){
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(this.itemList);
		list.addAll(this.itemListDay);
		return list;
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveItemsDay = SerializeHelper.collectionToString(this.itemListDay, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		this.itemListDay.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItemsDay, this.itemListDay);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
