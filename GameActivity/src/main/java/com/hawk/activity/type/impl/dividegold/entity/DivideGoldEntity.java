package com.hawk.activity.type.impl.dividegold.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**金币瓜分活动数据
 * @author Winder
 */
@Entity
@Table(name = "activity_divide_gold")
public class DivideGoldEntity extends AchieveActivityEntity implements IActivityDataEntity{
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
	
	/** 跨天刷新时间   */
    @IndexProp(id = 5)
	@Column(name = "lastRefreshTime", nullable = false)
	private long lastRefreshTime;
	
	/** 赠送次数   */
    @IndexProp(id = 6)
	@Column(name = "giveNum", nullable = false)
	private int giveNum;
	
	/** 索要的时间   */
    @IndexProp(id = 7)
	@Column(name = "askForTime", nullable = false)
	private long askForTime;
	
	/** 合成红包次数   */
    @IndexProp(id = 8)
	@Column(name = "compoundRedNum", nullable = false)
	private int compoundRedNum;
	
	/** 中奖记录   */
    @IndexProp(id = 9)
	@Column(name = "winRecord", nullable = false)
	private String winRecord;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	@Transient
	private List<Integer> winRecordList = new ArrayList<Integer>();
	
	public DivideGoldEntity() {
	}
	
	public DivideGoldEntity(String playerId) {
		this.playerId = playerId;
		this.achieveItems = "";
	}
	public DivideGoldEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.lastRefreshTime = HawkTime.getMillisecond();
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

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public long getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public void resetItemList(List<AchieveItem> retainList) {
		this.itemList = retainList;
		this.notifyUpdate();
	}

	public int getGiveNum() {
		return giveNum;
	}

	public void setGiveNum(int giveNum) {
		this.giveNum = giveNum;
	}

	public long getAskForTime() {
		return askForTime;
	}

	public void setAskForTime(long askForTime) {
		this.askForTime = askForTime;
	}
	
	public String getWinRecord() {
		return winRecord;
	}

	public void setWinRecord(String winRecord) {
		this.winRecord = winRecord;
	}

	public int getCompoundRedNum() {
		return compoundRedNum;
	}

	public void setCompoundRedNum(int compoundRedNum) {
		this.compoundRedNum = compoundRedNum;
	}

	public List<Integer> getWinRecordList() {
		return winRecordList;
	}

	public void setWinRecordList(List<Integer> winRecordList) {
		this.winRecordList = winRecordList;
	}
	public void addWinRecordList(Integer winRecord) {
		this.winRecordList.add(winRecord);
		this.notifyUpdate();
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		this.winRecordList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(Integer.class, this.winRecord, this.winRecordList);
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.winRecord = SerializeHelper.collectionToString(this.winRecordList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
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
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
		
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
}
