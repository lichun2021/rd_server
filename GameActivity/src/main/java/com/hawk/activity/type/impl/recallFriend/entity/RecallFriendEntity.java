package com.hawk.activity.type.impl.recallFriend.entity;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
*	
*	auto generate do not modified
*/
@Entity
@Table(name="activity_recall_friend")
public class RecallFriendEntity extends ActivityDataEntity {

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/***/
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=50)
	private String playerId;

	/***/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/***/
    @IndexProp(id = 4)
	@Column(name="achieveItems", nullable = false, length=1024)
	private String achieveItems;

	/***/
    @IndexProp(id = 5)
	@Column(name="recallNum", nullable = false, length=10)
	private int recallNum;
	/**
	 * 重置时间
	 */
    @IndexProp(id = 6)
	@Column(name="lastResetTime", nullable = false, length=19)
	private long lastResetTime;
	/**
	 * 登录天数记录
	 */
    @IndexProp(id = 7)
	@Column(name = "loginDays", nullable = false)
	private String loginDays;
	/**
	 * 每天召唤玩家数据
	 */
    @IndexProp(id = 8)
	@Column(name = "recallPlayer", nullable = false)
	private String recallPlayer;

	/***/
    @IndexProp(id = 9)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 10)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 11)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> achieveItemList = new CopyOnWriteArrayList<>();

	@Transient
	private List<String> recallPlayerList = new CopyOnWriteArrayList<>();
	
	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getAchieveItems() {
		return this.achieveItems; 
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public int getRecallNum() {
		return this.recallNum; 
	}

	public void setRecallNum(int recallNum) {
		this.recallNum = recallNum;
	}

	public long getCreateTime() {
		return this.createTime; 
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return this.invalid; 
	}

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
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieveItemList, SerializeHelper.ELEMENT_DELIMITER);
		this.recallPlayer = SerializeHelper.collectionToString(this.recallPlayerList, SerializeHelper.ATTRIBUTE_SPLIT);
		if(this.loginDays == null){
			this.loginDays = "";
		}
	}
	
	@Override
	public void afterRead() {
		List<AchieveItem> newList = new CopyOnWriteArrayList<>();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, newList);
		this.achieveItemList = newList;
		this.recallPlayerList = SerializeHelper.stringToList(String.class, this.recallPlayer, SerializeHelper.ATTRIBUTE_SPLIT);
		this.stringToLoginDaysList();
	}

	public long getLastResetTime() {
		return lastResetTime;
	}

	public void setLastResetTime(long lastResetTime) {
		this.lastResetTime = lastResetTime;
	}

	public List<AchieveItem> getAchieveItemList() {
		return achieveItemList;
	}

	public void setAchieveItemList(List<AchieveItem> achieveItemList) {
		this.achieveItemList = achieveItemList;
	}
	
	public void addItem(AchieveItem item) {
		this.achieveItemList.add(item);
		notifyUpdate();
	}

	@Override
	public void recordLoginDay() {
		int val = HawkTime.getYyyyMMddIntVal();
		if (getLoginDaysList().contains(val)) {
			return;
		}
		//跨天了,清召回玩家的记录
		this.recallPlayerList = new CopyOnWriteArrayList<>();
		super.recordLoginDay();
	}

	@Override
	public void setLoginDaysStr(String loginDays) {
		if(this.loginDays == null){
			
		}
		this.loginDays = loginDays;
	}

	@Override
	public String getLoginDaysStr() {
		return loginDays;
	}

	public void addRecallPlayerList(String targetPlayerId) {
		if (!recallPlayerList.contains(targetPlayerId)){
			recallPlayerList.add(targetPlayerId);
			notifyUpdate();
		}
	}
	public List<String> getRecallPlayerList() {
		return recallPlayerList;
	}

	public void setRecallPlayerList(List<String> recallPlayerList) {
		this.recallPlayerList = recallPlayerList;
	}
}
