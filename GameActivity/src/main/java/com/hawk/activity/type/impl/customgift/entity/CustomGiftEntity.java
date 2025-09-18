package com.hawk.activity.type.impl.customgift.entity;

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

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.customgift.PurchaseItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_custom_gift")
public class CustomGiftEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	
	// 购买信息 giftId_purchaseTime_selectedBox,giftId_purchaseTime_selectedBox
    @IndexProp(id = 4)
	@Column(name = "purchaseItems", nullable = false)
	private String purchaseItems;

    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@IndexProp(id = 8)
	@Column(name = "count", nullable = false)
	private int count;

	@IndexProp(id = 9)
	@Column(name = "freeGet", nullable = false)
	private String freeGet = "";

	@IndexProp(id = 10)
	@Column(name = "resetTime", nullable = false)
	private long resetTime;
	
	@Transient
	private List<PurchaseItem> itemList = new CopyOnWriteArrayList<PurchaseItem>();

	/** 兑换数量 */
	@Transient
	private Map<Integer, Long> freeGetMap = new HashMap<>();
	
	public CustomGiftEntity() {
	}
	
	public CustomGiftEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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
	
	public String getPurchaseItems() {
		return purchaseItems;
	}

	public void setPurchaseItems(String purchaseItems) {
		this.purchaseItems = purchaseItems;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getResetTime() {
		return resetTime;
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public Map<Integer, Long> getFreeGetMap() {
		return freeGetMap;
	}

	public void addItem(PurchaseItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<PurchaseItem> getItemList() {
		return itemList;
	}
	
	public PurchaseItem getPurchaseItem(int giftId) {
		for (PurchaseItem item : itemList) {
			if (item.getGiftId() == giftId) {
				return item;
			}
		}
		
		return null;
	}

	@Override
	public void beforeWrite() {
		this.purchaseItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.freeGet = SerializeHelper.mapToString(this.freeGetMap);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(PurchaseItem.class, this.purchaseItems, this.itemList);
		this.freeGetMap = SerializeHelper.stringToMap(this.freeGet, Integer.class, Long.class);
	}

}
