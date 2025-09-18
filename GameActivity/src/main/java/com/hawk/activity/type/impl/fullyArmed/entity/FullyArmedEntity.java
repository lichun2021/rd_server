package com.hawk.activity.type.impl.fullyArmed.entity;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkException;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_fully_armed")
public class FullyArmedEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 5)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 6)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    @IndexProp(id = 7)
	@Column(name = "shopItems", nullable = false)
	private String shopItems;
	
    @IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/**已经探测过最大id*/
    @IndexProp(id = 9)
	@Column(name = "searchId", nullable = false)
	private int searchId;
	
	@Transient
	private Map<Integer,Integer> shopItemsMap = new HashMap<>();
	public FullyArmedEntity(){
		searchId = 0;
		achieveItems = "";
		shopItems = "";
	}
	
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public FullyArmedEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	public Map<Integer,Integer> getBuyRecordMap(){
		return this.shopItemsMap;
	}
	
	public int getBuyTimes( int id ){
		Integer val = shopItemsMap.get(id);
		if(null != val){
			return val;
		}
		return 0;
	}
	
	public void addBuyTimes(int id, int v){
		Integer val = shopItemsMap.get(id);
		if(null != val){
			shopItemsMap.put(id, val + v );
			return;
		}
		shopItemsMap.put(id, v);
	}
	
	public int getSearchId(){
		return searchId;
	}
	public void setSearchId(int val){
		this.searchId = val;
	}
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
	}
	
	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	@Override
	public void beforeWrite() {
		try{
			this.shopItems = SerializeHelper.mapToString(shopItemsMap);
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try {
			this.shopItemsMap = SerializeHelper.stringToMap(shopItems, Integer.class, Integer.class);
			this.itemList.clear();
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
