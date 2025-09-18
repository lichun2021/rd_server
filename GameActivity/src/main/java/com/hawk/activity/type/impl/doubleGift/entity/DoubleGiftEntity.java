package com.hawk.activity.type.impl.doubleGift.entity;

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
@Table(name = "activity_double_gift")
public class DoubleGiftEntity extends AchieveActivityEntity implements IActivityDataEntity {

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

	// 购买信息giftId_rewardId_buyTime
    @IndexProp(id = 4)
	@Column(name = "doubleGiftItems", nullable = false)
	private String doubleGiftItems;

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
	List<DoubleGiftItem> doubleGiftList = new ArrayList<DoubleGiftItem>();
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

	public DoubleGiftEntity() {
	}

	public DoubleGiftEntity(String playerId, int termId) {
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

	public void setDoubleGiftItems(String doubleGiftItems) {
		this.doubleGiftItems = doubleGiftItems;
	}

	public long getLatestPurchaseTime() {
		return latestPurchaseTime;
	}

	public void setLatestPurchaseTime(long latestPurchaseTime) {
		this.latestPurchaseTime = latestPurchaseTime;
	}
	
	public List<DoubleGiftItem> getDoubleGiftList() {
		return doubleGiftList;
	}

	public void setDoubleGiftList(List<DoubleGiftItem> doubleGiftList) {
		this.doubleGiftList = doubleGiftList;
	}

	public void addDoubleGift(DoubleGiftItem item) {
		DoubleGiftItem oldItem = getDoubleGiftItem(doubleGiftList, item.getGiftId());
		if (oldItem != null) {
			oldItem.setRewardId(item.getRewardId());
		} else {
			doubleGiftList.add(item);
		}
		this.notifyUpdate();
	}

	private DoubleGiftItem getDoubleGiftItem(List<DoubleGiftItem> doubleGiftList, int giftId) {
		for (DoubleGiftItem item : doubleGiftList) {
			if (item.getGiftId() == giftId) {
				return item;
			}
		}
		return null;
	}
	
	public DoubleGiftItem getDoubleGiftItem(int giftId) {
		if (null != doubleGiftList) {
			return getDoubleGiftItem(doubleGiftList, giftId);
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
			this.doubleGiftItems = SerializeHelper.collectionToString(this.doubleGiftList, SerializeHelper.ELEMENT_DELIMITER);
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);			
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try{
			this.itemList.clear();
			this.doubleGiftList.clear();
			SerializeHelper.stringToList(DoubleGiftItem.class, this.doubleGiftItems, this.doubleGiftList);
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

}
