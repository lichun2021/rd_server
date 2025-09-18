package com.hawk.activity.type.impl.AnniversaryGfit.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_anniversary_gift")
public class AnniversaryGiftEntity  extends ActivityDataEntity{

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
	@Column(name = "initTime", nullable = false)
    private long initTime;
    
	/** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    /** 活动成就项数据 */
    @IndexProp(id = 6)
	@Column(name = "achieveItemsDaily", nullable = false)
	private String achieveItemsDaily;

    
    @IndexProp(id = 7)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
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
	private List<AchieveItem> itemListDaily = new CopyOnWriteArrayList<AchieveItem>();

	public AnniversaryGiftEntity() {
		
	}
	
	public AnniversaryGiftEntity(String playerId) {
		this.playerId = playerId;
		this.achieveItems = "";
		this.achieveItemsDaily = "";
		this.loginDays = "";
	}
	
	public AnniversaryGiftEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.achieveItemsDaily = "";
		this.loginDays = "";
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveItemsDaily = SerializeHelper.collectionToString(this.itemListDaily, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		this.itemListDaily.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItemsDaily, this.itemListDaily);
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
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	
	public List<AchieveItem> getItemListDaily() {
		return itemListDaily;
	}
	
	
	public void setItemListDaily(List<AchieveItem> itemListDaily) {
		this.itemListDaily = itemListDaily;
	}
	
	
	public List<AchieveItem> getAllAchieveList(){
		List<AchieveItem> list = new ArrayList<>();
		list.addAll(this.itemList);
		list.addAll(this.itemListDaily);
		return list;
	}
    
	
	@Override
	public List<Integer> getLoginDaysList() {
		return super.getLoginDaysList();
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

	
    @Override
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }
	    
    @Override
	public String getLoginDaysStr() {
		return this.loginDays;
	}
	
	
}
