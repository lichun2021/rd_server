package com.hawk.activity.type.impl.soldierExchange.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Table(name = "activity_soldier_exchange")
public class SoldierExchangeActivityEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	@Column(name = "shopItems", nullable = false)
	private String shopItems;

	@IndexProp(id = 5)
	@Column(name = "exchangeType", nullable = false)
	private int exchangeType;

	/** 活动成就项数据 */
	@IndexProp(id = 6)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

	/** 历史转换记录id */
	@IndexProp(id = 7)
	@Column(name = "histor", nullable = false)
	private String histor;

	@IndexProp(id = 18)
	@Column(name = "createTime", nullable = false)
	private long createTime;

	@IndexProp(id = 19)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

	@IndexProp(id = 20)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<AchieveItem> itemList = new ArrayList<>();

	@Transient
	private Map<Integer, Integer> shopItemsMap = new HashMap<>();

	@Transient
	private List<String> historList = new ArrayList<>();
	
	@Transient
	private long coolTime;
	
	public SoldierExchangeActivityEntity() {
	}

	public SoldierExchangeActivityEntity(String playerId) {
		this.playerId = playerId;
		this.histor = "";
	}

	public SoldierExchangeActivityEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.histor = "";
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

	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}

	@Override
	public void afterRead() {
		this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
		this.shopItemsMap = SerializeHelper.stringToMap(shopItems, Integer.class, Integer.class);
		this.historList = SerializeHelper.stringToList(String.class, this.histor);
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList);
		this.shopItems = SerializeHelper.mapToString(shopItemsMap);
		this.histor = SerializeHelper.collectionToString(this.historList);
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
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
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
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public String getHistor() {
		return histor;
	}

	public void setHistor(String histor) {
		this.histor = histor;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	public void addShopItems(int id, int count) {
		if (id <= 0 || count <= 0) {
			return;
		}
		Integer v = this.shopItemsMap.get(id);
		if (null != v) {
			v += count;
			this.shopItemsMap.put(id, v);
			return;
		}
		this.shopItemsMap.put(id, count);
	}

	public Map<Integer, Integer> getShopItems() {
		return this.shopItemsMap;
	}

	public int getShopItemVal(int id) {
		Integer v = this.shopItemsMap.get(id);
		if (null == v) {
			return 0;
		}
		return v;
	}

	public int getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(int exchangeType) {
		this.exchangeType = exchangeType;
	}

	public Map<Integer, Integer> getShopItemsMap() {
		return shopItemsMap;
	}

	public void setShopItemsMap(Map<Integer, Integer> shopItemsMap) {
		this.shopItemsMap = shopItemsMap;
	}

	public List<String> getHistorList() {
		return historList;
	}

	public void setHistorList(List<String> historList) {
		this.historList = historList;
	}

	public void setShopItems(String shopItems) {
		this.shopItems = shopItems;
	}

	public long getCoolTime() {
		return coolTime;
	}

	public void setCoolTime(long coolTime) {
		this.coolTime = coolTime;
	}

}
