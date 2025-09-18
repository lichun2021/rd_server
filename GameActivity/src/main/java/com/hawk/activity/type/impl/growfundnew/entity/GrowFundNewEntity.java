package com.hawk.activity.type.impl.growfundnew.entity;

import java.util.ArrayList;
import java.util.List;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_growfund_new")
public class GrowFundNewEntity  extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 购买成长基金的时间 */
    @IndexProp(id = 4)
	@Column(name = "fundBuyTime", unique = true, nullable = false)
	private long fundBuyTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList;

	public GrowFundNewEntity() {
		this.itemList = new ArrayList<>();
	}
	
	public GrowFundNewEntity(String playerId) {
		this.playerId = playerId;
		this.achieveItems = "";
		this.itemList = new ArrayList<>();
	}
	
	public GrowFundNewEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.itemList = new ArrayList<>();
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
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public long getFundBuyTime() {
		return fundBuyTime;
	}
	
	public void setFundBuyTime(long fundBuyTime) {
		this.fundBuyTime = fundBuyTime;
	}
	
	public boolean isBuy() {
		return fundBuyTime > 0;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
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
