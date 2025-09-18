package com.hawk.activity.type.impl.honourMobilize.entity;

import java.util.ArrayList;
import java.util.List;

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
@Table(name="activity_honour_mobilize")
public class HonourMobilizeEntity extends ActivityDataEntity{

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
  	@Column(name = "chooseId", nullable = false)
  	private int chooseId;
    
    @IndexProp(id = 5)
	@Column(name = "freeCount", nullable = false)
	private int freeCount;
	
    @IndexProp(id = 6)
	@Column(name = "lotteryCount", nullable = false)
	private int lotteryCount;
    
    
    @IndexProp(id = 7)
	@Column(name = "lotteryTotalCount", nullable = false)
	private int lotteryTotalCount;
    
    @IndexProp(id = 8)
    @Column(name = "loginDays", nullable = false)
    private String loginDays;
    
   
	/** 活动成就项数据 */
    @IndexProp(id = 9)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    @IndexProp(id = 10)
   	@Column(name = "initTime", nullable = false)
    private long initTime;
	
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	
	
	public HonourMobilizeEntity() {
		
	}
	
	public HonourMobilizeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.freeCount = 0;
		this.lotteryCount = 0;
		this.achieveItems = "";
		this.loginDays = "";
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
	
	public String getPlayerId() {
		return playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public int getChooseId() {
		return chooseId;
	}
	
	public void setChooseId(int chooseId) {
		this.chooseId = chooseId;
	}
	
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public long getInitTime() {
		return initTime;
	}
	
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	
	public int getFreeCount() {
		return freeCount;
	}
	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}
	
	public void addFreeCount(int count){
		this.freeCount += count;
		this.notifyUpdate();
	}
	
	public int getLotteryCount() {
		return lotteryCount;
	}
	
	
	public void addLotteryCount(int count){
		this.lotteryCount += count;
		this.notifyUpdate();
		
	}
	
	
	public void setLotteryCount(int lotteryCount) {
		this.lotteryCount = lotteryCount;
	}

	public int getDailyTotalLotteryCount(){
		return this.lotteryCount + this.freeCount;
	}
	
	public int getLotteryTotalCount() {
		return lotteryTotalCount;
	}
	
	public void setLotteryTotalCount(int lotteryTotalCount) {
		this.lotteryTotalCount = lotteryTotalCount;
	}
	
	
	public void addLotteryTotalCount(int count) {
		this.lotteryTotalCount += count;
		this.notifyUpdate();
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
