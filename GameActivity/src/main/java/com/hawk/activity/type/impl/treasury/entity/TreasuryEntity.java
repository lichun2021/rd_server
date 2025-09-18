package com.hawk.activity.type.impl.treasury.entity; 

import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
*	金币宝藏
*	auto generate do not modified
*/
@Entity
@Table(name="activity_treasury")
public class TreasuryEntity extends HawkDBEntity implements IActivityDataEntity{

	/**主键*/
	@Id	
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;

	/**周期ID*/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/**存入信息*/
    @IndexProp(id = 4)
	@Column(name="storageInfo", nullable = false, length=256)
	private String storageInfo;

	/**领取信息*/
    @IndexProp(id = 5)
	@Column(name="receivedInfo", nullable = false, length=256)
	private String receivedInfo;

	/**消耗信息*/
    @IndexProp(id = 6)
	@Column(name="costInfo", length=256)
	private String costInfo;

	/**创建时间*/
    @IndexProp(id = 7)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/**更新时间*/
    @IndexProp(id = 8)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/**逻辑标志位*/
    @IndexProp(id = 9)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;

	/** complex type @storageInfo*/
	@Transient
	private Map<Integer, Integer> storageInfoMap;

	/** complex type @receivedInfo*/
	@Transient
	private Map<Integer, Integer> receivedInfoMap;

	/** complex type @costInfo*/
	@Transient
	private Map<Integer, Integer> costInfoMap;

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getStorageInfo() {
		return this.storageInfo; 
	}

	public void setStorageInfo(String storageInfo) {
		this.storageInfo = storageInfo;
	}

	public String getReceivedInfo() {
		return this.receivedInfo; 
	}

	public void setReceivedInfo(String receivedInfo) {
		this.receivedInfo = receivedInfo;
	}

	public String getCostInfo() {
		return this.costInfo; 
	}

	public void setCostInfo(String costInfo) {
		this.costInfo = costInfo;
	}

	public long getCreateTime() {
		return this.createTime; 
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return this.invalid; 
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public Map<Integer, Integer> getStorageInfoMap() {
		return this.storageInfoMap; 
	}

	public void setStorageInfoMap(Map<Integer, Integer> storageInfoMap) {
		this.storageInfoMap = storageInfoMap;
	}

	public Map<Integer, Integer> getReceivedInfoMap() {
		return this.receivedInfoMap; 
	}

	public void setReceivedInfoMap(Map<Integer, Integer> receivedInfoMap) {
		this.receivedInfoMap = receivedInfoMap;
	}

	public Map<Integer, Integer> getCostInfoMap() {
		return this.costInfoMap; 
	}

	public void setCostInfoMap(Map<Integer, Integer> costInfoMap) {
		this.costInfoMap = costInfoMap;
	}

	@Override
	public void afterRead() {		
		this.storageInfoMap = SerializeHelper.stringToMap(storageInfo, Integer.class, Integer.class);
		this.receivedInfoMap = SerializeHelper.stringToMap(receivedInfo, Integer.class, Integer.class);
		this.costInfoMap = SerializeHelper.stringToMap(costInfo, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {		
		this.storageInfo = SerializeHelper.mapToString(storageInfoMap);
		this.receivedInfo = SerializeHelper.mapToString(receivedInfoMap);
		this.costInfo = SerializeHelper.mapToString(costInfoMap);
	}

	public void addStorageInfo(Integer key, Integer value) {
		this.storageInfoMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeStorageInfo(Integer key) {
		this.storageInfoMap.remove(key);
		this.notifyUpdate();
	}
	public void addReceivedInfo(Integer key, Integer value) {
		this.receivedInfoMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeReceivedInfo(Integer key) {
		this.receivedInfoMap.remove(key);
		this.notifyUpdate();
	}
	public void addCostInfo(Integer key, Integer value) {
		this.costInfoMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeCostInfo(Integer key) {
		this.costInfoMap.remove(key);
		this.notifyUpdate();
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
