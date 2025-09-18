package com.hawk.activity.type.impl.redrecharge.entity;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "activity_red_recharge")
public class HappyRedRechargeEntity extends ActivityDataEntity{

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
	@Column(name = "loginDays", nullable = false)
	private String loginDays;

	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 6)
	@Column(name = "rechargeItems", nullable = false)
	private String rechargeItems;
	
    @IndexProp(id = 7)
	@Column(name = "score", nullable = false)
	private int score;

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
	private List<HappyRedRechargeItem> rechargeItemList = new ArrayList<HappyRedRechargeItem>(); 

	public HappyRedRechargeEntity() {
	}
	
	public HappyRedRechargeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.loginDays = "";
		this.achieveItems = "";
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
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
		this.notifyUpdate();
	}
	
	public void addRechargeItem(HappyRedRechargeItem rechargeItem) {
		for (HappyRedRechargeItem item : rechargeItemList) {
			if (item.getCfgId() == rechargeItem.getCfgId()) {
				item.setBuyCount(rechargeItem.getBuyCount());
				item.setLatestBuyTime(rechargeItem.getLatestBuyTime());
				notifyUpdate();
				return;
			}
		}
		
		rechargeItemList.add(rechargeItem);
		notifyUpdate();
	}
	
	public HappyRedRechargeItem getRechargeItem(int cfgId) {
		for (HappyRedRechargeItem item : rechargeItemList) {
			if (item.getCfgId() == cfgId) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<HappyRedRechargeItem> getRechargeItems() {
		return rechargeItemList;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.rechargeItems = SerializeHelper.collectionToString(this.rechargeItemList, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		this.rechargeItemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(HappyRedRechargeItem.class, this.rechargeItems, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, this.rechargeItemList);
		this.stringToLoginDaysList();
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
		return loginDays;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
}
