package com.hawk.activity.type.impl.pioneergift.entity;

import java.util.ArrayList;
import java.util.List;
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
@Table(name = "activity_pioneer_gift")
public class PioneerGiftEntity extends AchieveActivityEntity implements IActivityDataEntity {

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

	// 购买信息 type_giftId_purchaseTime，type_giftId_purchaseTime
    @IndexProp(id = 4)
	@Column(name = "purchaseItems", nullable = false)
	private String purchaseItems;

	// 累计购买天数
    @IndexProp(id = 5)
	@Column(name = "accDay", nullable = false)
	private int accDay;
	
	// 最近购买时间
    @IndexProp(id = 6)
	@Column(name = "latestPurchaseTime", nullable = false)
	private long latestPurchaseTime;

	// 领取免费礼包的时间
    @IndexProp(id = 7)
	@Column(name = "freeTakenTime", nullable = false)
	private long freeTakenTime;

	// 累计购买成就
    @IndexProp(id = 8)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	List<PurchaseItem> itemPurchaseList = new ArrayList<PurchaseItem>();
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

	public PioneerGiftEntity() {
	}

	public PioneerGiftEntity(String playerId, int termId) {
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

	public long getFreeTakenTime() {
		return freeTakenTime;
	}

	public void setFreeTakenTime(long freeTakenTime) {
		this.freeTakenTime = freeTakenTime;
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

	public long getLatestPurchaseTime() {
		return latestPurchaseTime;
	}

	public void setLatestPurchaseTime(long latestPurchaseTime) {
		this.latestPurchaseTime = latestPurchaseTime;
	}
	
	public List<PurchaseItem> getItemPurchaseList() {
		return itemPurchaseList;
	}

	public void addPurchaseItem(PurchaseItem item) {
		PurchaseItem oldItem = getPurchaseItem(itemPurchaseList, item.getType());
		if (oldItem != null) {
			oldItem.setGiftId(item.getGiftId());
		} else {
			itemPurchaseList.add(item);
		}
		this.notifyUpdate();
	}

	private PurchaseItem getPurchaseItem(List<PurchaseItem> itemPurchaseList, int type) {
		for (PurchaseItem item : itemPurchaseList) {
			if (item.getType() == type) {
				return item;
			}
		}
		return null;
	}
	
	public PurchaseItem getPurchaseItem(int type) {
		if (null != itemPurchaseList) {
			return getPurchaseItem(itemPurchaseList, type);
		}
		
		return null;
	}

	public String getAchieveItems() {
		return achieveItems;
	}
	
	public int getAccDay(){
		return this.accDay;
	}
	
	public void setAccDay(int days){
		this.accDay = days;
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
			this.purchaseItems = SerializeHelper.collectionToString(itemPurchaseList, SerializeHelper.ELEMENT_DELIMITER);
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);			
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try{
			this.itemList.clear();
			this.itemPurchaseList.clear();
			SerializeHelper.stringToList(PurchaseItem.class, this.purchaseItems, this.itemPurchaseList);
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}


}
