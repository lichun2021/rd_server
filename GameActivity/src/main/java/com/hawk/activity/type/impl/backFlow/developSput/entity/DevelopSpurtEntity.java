package com.hawk.activity.type.impl.backFlow.developSput.entity;

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
@Table(name = "activity_develop_spurt")
public class DevelopSpurtEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	
	/**
	 * 回流次数
	 */
    @IndexProp(id = 4)
	@Column(name = "backCount", nullable = false)
	private int backCount;
	
	/**
	 * 登录时间
	 */
    @IndexProp(id = 5)
	@Column(name = "loginTime", nullable = false)
	private long loginTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 6)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	
	
	/** 活动成就项数据 */
    @IndexProp(id = 7)
	@Column(name = "signInDays", nullable = false)
	private int signInDays;
	

	/** 签到时间 */
    @IndexProp(id = 8)
	@Column(name = "signInTime", nullable = false)
	private long signInTime;
	
	/** 累计登录进阶礼物解锁时间 */
    @IndexProp(id = 9)
	@Column(name = "unlockTime", nullable = false)
	private long unlockTime;
	
	
	
	/** 活动成就项数据 */
    @IndexProp(id = 10)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 11)
	@Column(name = "backType", nullable = false)
	private int backType;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 12)
	@Column(name = "overTime", nullable = false)
	private long overTime;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 13)
	@Column(name = "startTime", nullable = false)
	private long startTime;
	

    @IndexProp(id = 14)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	

    @IndexProp(id = 15)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	

    @IndexProp(id = 16)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	
	@Transient
	private List<AchieveItem> achieveItemList = new CopyOnWriteArrayList<AchieveItem>();
	
	
	
	public DevelopSpurtEntity() {
	}

	public DevelopSpurtEntity(String playerId) {
		this.playerId = playerId;
		this.achieveItems = "";
	}
	
	public DevelopSpurtEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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
	

	

	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
	}

	
	
	

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public int getSignInDays() {
		return signInDays;
	}

	public void setSignInDays(int signInDays) {
		this.signInDays = signInDays;
	}
	
	public long getSignInTime() {
		return signInTime;
	}

	public void setSignInTime(long signInTime) {
		this.signInTime = signInTime;
	}

	public long getUnlockTime() {
		return unlockTime;
	}

	public void setUnlockTime(long unlockTime) {
		this.unlockTime = unlockTime;
	}

	public String getAchieveItems() {
		return achieveItems;
	}

	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	
	public List<AchieveItem> getAchieveItemList() {
		return achieveItemList;
	}
	

	public void setAchieveItemList(List<AchieveItem> achieveItemList) {
		this.achieveItemList = achieveItemList;
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
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieveItemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.achieveItemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieveItemList);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	
	
	public void resetAchieveItemList(List<AchieveItem> achieveItemList) {
		this.achieveItemList = achieveItemList;
		this.notifyUpdate();
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

	public int getBackType() {
		return backType;
	}

	public void setBackType(int backType) {
		this.backType = backType;
	}
	
	
}
