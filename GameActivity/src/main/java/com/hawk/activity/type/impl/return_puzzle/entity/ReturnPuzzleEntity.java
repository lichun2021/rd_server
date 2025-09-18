package com.hawk.activity.type.impl.return_puzzle.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
@Table(name = "activity_return_puzzle")
public class ReturnPuzzleEntity extends AchieveActivityEntity implements IActivityDataEntity {

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
	@Column(name = "curDay", nullable = false)
	private int curDay;

    @IndexProp(id = 5)
	@Column(name = "score", nullable = false)
	private int score;

	/** 活动成就项数据 */
    @IndexProp(id = 6)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 7)
	@Column(name = "achieveBoxItems", nullable = false)
	private String achieveBoxItems;
	
    @IndexProp(id = 8)
	@Column(name = "achieveShareItems", nullable = false)
	private String achieveShareItems;
	
    @IndexProp(id = 9)
	@Column(name = "nextTime", nullable = false)
	private long nextTime;
	
    @IndexProp(id = 10)
	@Column(name = "loginDay", nullable = false)
	private int loginDay;
	/**
	 * 玩家回归天数
	 */
    @IndexProp(id = 11)
	@Column(name = "lossDays", nullable = false)
	private int lossDays;
	/**
	 * 玩家回归VIP
	 */
    @IndexProp(id = 12)
	@Column(name = "lossVip", nullable = false)
	private int lossVip;
	/**
	 * 玩家回归时间 配置ID
	 */
    @IndexProp(id = 13)
	@Column(name = "backType", nullable = false)
	private int backType;
	/**
	 * 回流次数
	 */
    @IndexProp(id = 14)
	@Column(name = "backCount", nullable = false)
	private int backCount;
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 15)
	@Column(name = "overTime", nullable = false)
	private long overTime;
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 16)
	@Column(name = "startTime", nullable = false)
	private long startTime;	
	
	
    @IndexProp(id = 17)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 18)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 19)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<AchieveItem> boxList = new CopyOnWriteArrayList<AchieveItem>();
	
	@Transient
	private List<AchieveItem> shareList = new CopyOnWriteArrayList<AchieveItem>();

	public ReturnPuzzleEntity() {
	}

	public ReturnPuzzleEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.achieveBoxItems = "";
		this.loginDay=1;
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

	public int getCurDay() {
		return curDay;
	}

	public void setCurDay(int curDay) {
		this.curDay = curDay;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
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

	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public List<AchieveItem> getAllList(){
		List<AchieveItem> allList = new ArrayList<>();
		allList.addAll(itemList);
		allList.addAll(boxList);
		allList.addAll(shareList);
		return allList;
	}
	
	public List<AchieveItem> getBoxList() {
		return boxList;
	}

	public void setBoxList(List<AchieveItem> boxList) {
		this.boxList = boxList;
		this.notifyUpdate();
	}
	

	public List<AchieveItem> getShareList() {
		return shareList;
	}

	public void setShareList(List<AchieveItem> shareList) {
		this.shareList = shareList;
		this.notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveBoxItems = SerializeHelper.collectionToString(this.boxList, SerializeHelper.ELEMENT_DELIMITER);
		this.achieveShareItems = SerializeHelper.collectionToString(this.shareList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		this.boxList.clear();
		this.shareList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(AchieveItem.class, this.achieveBoxItems, this.boxList);
		SerializeHelper.stringToList(AchieveItem.class, this.achieveShareItems, this.shareList);
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public long getNextTime() {
		return nextTime;
	}

	public void setNextTime(long nextTime) {
		this.nextTime = nextTime;
	}

	public int getLossDays() {
		return lossDays;
	}

	public void setLossDays(int lossDays) {
		this.lossDays = lossDays;
	}

	public int getLossVip() {
		return lossVip;
	}

	public void setLossVip(int lossVip) {
		this.lossVip = lossVip;
	}

	public int getBackType() {
		return backType;
	}

	public void setBackType(int backType) {
		this.backType = backType;
	}

	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getLoginDay() {
		return loginDay;
	}

	public void setLoginDay(int loginDay) {
		this.loginDay = loginDay;
	}
}
