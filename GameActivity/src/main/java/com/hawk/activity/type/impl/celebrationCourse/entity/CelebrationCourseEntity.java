package com.hawk.activity.type.impl.celebrationCourse.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
/**
 * 周年历程
 * @author luke
 */
@Entity
@Table(name = "activity_celebration_course")
public class CelebrationCourseEntity  extends HawkDBEntity implements IActivityDataEntity {

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
	@Column(name = "signNumber", nullable = false)
	private int signNumber;
	
    @IndexProp(id = 5)
	@Column(name = "signTime", nullable = false)
	private long signTime;
	
    @IndexProp(id = 6)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 7)
	@Column(name = "shareIds", nullable = false)
	private String shareIds;
	
    @IndexProp(id = 8)
	@Column(name = "shareReward", nullable = false)
	private String shareReward;

    @IndexProp(id = 9)
	@Column(name = "shareTime", nullable = false)
	private long shareTime;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	/** 成就任务列表*/
	@Transient
	private List<AchieveItem> achieveList = new CopyOnWriteArrayList<AchieveItem>();
	/**
	 * 分享id
	 */
	@Transient
	private List<Integer> shareIdsList = new CopyOnWriteArrayList<Integer>();
	/**
	 * 分享奖励
	 */
	@Transient
	private List<Integer> shareRewardList = new CopyOnWriteArrayList<Integer>();
	
	public CelebrationCourseEntity() {
	}
	
	public CelebrationCourseEntity(String playerId, int termId) {
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	
	public List<AchieveItem> getAchieveList() {
		return achieveList;
	}

	public void setAchieveList(List<AchieveItem> achieveList) {
		this.achieveList = achieveList;
		this.notifyUpdate();
	}

	
	public String getShareIds() {
		return shareIds;
	}

	public void setShareIds(String shareIds) {
		this.shareIds = shareIds;
	}

	public String getShareReward() {
		return shareReward;
	}

	public void setShareReward(String shareReward) {
		this.shareReward = shareReward;
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
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.achieveList, SerializeHelper.ELEMENT_DELIMITER);
		this.shareIds = SerializeHelper.collectionToString(this.shareIdsList, SerializeHelper.ELEMENT_DELIMITER);
		this.shareReward = SerializeHelper.collectionToString(this.shareRewardList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.achieveList.clear();
		this.shareIdsList.clear();
		this.shareRewardList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieveList);
		SerializeHelper.stringToList(Integer.class, this.shareIds, this.shareIdsList);
		SerializeHelper.stringToList(Integer.class, this.shareReward, this.shareRewardList);
	}

	public void resetItemList(List<AchieveItem> achieveList) {
		this.achieveList = achieveList;
		this.notifyUpdate();
	}

	public int getSignNumber() {
		return signNumber;
	}

	public void setSignNumber(int signNumber) {
		this.signNumber = signNumber;
	}

	public long getSignTime() {
		return signTime;
	}
	
	public void addSignNumber(){
		this.signNumber ++;
	}

	public void setSignTime(long signTime) {
		this.signTime = signTime;
	}

	public List<Integer> getShareIdsList() {
		return shareIdsList;
	}

	public void setShareIdsList(List<Integer> shareIdsList) {
		this.shareIdsList = shareIdsList;
	}

	public List<Integer> getShareRewardList() {
		return shareRewardList;
	}

	public void setShareRewardList(List<Integer> shareRewardList) {
		this.shareRewardList = shareRewardList;
	}

	public long getShareTime() {
		return shareTime;
	}

	public void setShareTime(long shareTime) {
		this.shareTime = shareTime;
	}
	
}
