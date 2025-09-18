package com.hawk.activity.type.impl.aftercompetition.entity;

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
import com.hawk.activity.type.impl.aftercompetition.data.RecGiftInfo;
import com.hawk.activity.type.impl.aftercompetition.data.SendGiftInfo;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_after_competition")
public class AfterCompetitionEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 活动成就项数据 */
    @IndexProp(id = 4)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    /**
     * 上一次致敬的时间
     */
    @IndexProp(id = 5)
  	@Column(name = "homageTime", nullable = false)
  	private long homageTime;
    
    @IndexProp(id = 6)
  	@Column(name = "giftInfo", nullable = false)
  	private String giftInfo;
    
	@IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
    
	@Transient
	private List<AchieveItem> itemList = new ArrayList<>();
	
	@Transient
	private List<ACGiftItem> giftList = new ArrayList<>();
	
	@Transient
	private List<SendGiftInfo> sendGiftRecordList = new ArrayList<>();
	
	@Transient
	private List<RecGiftInfo> recGiftRecordList = new ArrayList<>();
	

	public AfterCompetitionEntity() {
		this.itemList = new ArrayList<>();
	}
	
	public AfterCompetitionEntity(String playerId) {
		this.playerId = playerId;
		this.itemList = new ArrayList<>();
	}
	
	public AfterCompetitionEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.itemList = new ArrayList<>();
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.giftInfo = SerializeHelper.collectionToString(this.giftList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		this.giftList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(ACGiftItem.class, this.giftInfo, this.giftList);
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
		this.notifyUpdate();
	}
	
	public long getHomageTime() {
		return homageTime;
	}

	public void setHomageTime(long homageTime) {
		this.homageTime = homageTime;
	}
	
	public List<ACGiftItem> getGiftList() {
		return giftList;
	}
	
	public ACGiftItem getGiftInfo(int giftId) {
		for (ACGiftItem gift : giftList) {
			if (gift.getGiftId() == giftId) {
				return gift;
			}
		}
		return null;
	}
	
	public ACGiftItem addGiftItem(int giftId) {
		ACGiftItem giftItem = ACGiftItem.valueOf(giftId);
		giftList.add(giftItem);
		this.notifyUpdate();
		return giftItem;
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
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public List<SendGiftInfo> getSendGiftRecordList() {
		return sendGiftRecordList;
	}
	
	public void addSendGiftRecord(SendGiftInfo record) {
		sendGiftRecordList.add(record);
	}

	public List<RecGiftInfo> getRecGiftRecordList() {
		return recGiftRecordList;
	}

	public void addRecGiftRecord(RecGiftInfo record) {
		recGiftRecordList.add(record);
	}
	
}
