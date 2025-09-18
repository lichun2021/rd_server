package com.hawk.activity.type.impl.questionShare.entity;

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
@Table(name = "activity_question_share")
public class QuestionShareEntity extends AchieveActivityEntity implements IActivityDataEntity {
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
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/** 活动周期累计分享 **/
    @IndexProp(id = 8)
	@Column(name = "shareAmount", nullable = false)
	private int shareAmount;

	/** 今日领取每日奖励 **/
    @IndexProp(id = 9)
	@Column(name = "dailyRewarded", nullable = false)
	private int dailyRewarded;
	
	/** 当天题目 **/
    @IndexProp(id = 10)
	@Column(name = "dayQuestion", nullable = false)
	private String dayQuestion;

	/** 当天回答 **/
    @IndexProp(id = 11)
	@Column(name = "dayAnswer", nullable = false)
	private String dayAnswer;

	/** 当天分享 **/
    @IndexProp(id = 12)
	@Column(name = "dayShare", nullable = false)
	private String dayShare;

	/** 已经领取 **/
    @IndexProp(id = 13)
	@Column(name = "rewards", nullable = false)
	private String rewards;

	@Transient
	private List<Integer> dayQuestionList = new ArrayList<Integer>();

	@Transient
	private List<Integer> dayAnswerList = new ArrayList<Integer>();

	@Transient
	private List<Integer> dayShareList = new ArrayList<Integer>();

	@Transient
	private List<Integer> rewardsList = new ArrayList<Integer>();

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	public QuestionShareEntity() {
	}

	public QuestionShareEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.shareAmount = 0;
		this.dailyRewarded = 0;
	}

	@Override
	public void beforeWrite() {
		dayQuestion = SerializeHelper.collectionToString(dayQuestionList, SerializeHelper.ATTRIBUTE_SPLIT);
		dayAnswer = SerializeHelper.collectionToString(dayAnswerList, SerializeHelper.ATTRIBUTE_SPLIT);
		dayShare = SerializeHelper.collectionToString(dayShareList, SerializeHelper.ATTRIBUTE_SPLIT);
		rewards = SerializeHelper.collectionToString(rewardsList, SerializeHelper.ATTRIBUTE_SPLIT);
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		dayQuestionList = SerializeHelper.cfgStr2List(dayQuestion);
		dayAnswerList = SerializeHelper.cfgStr2List(dayAnswer);
		dayShareList = SerializeHelper.cfgStr2List(dayShare);
		rewardsList = SerializeHelper.cfgStr2List(rewards);
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public void incShare() {
		this.shareAmount++;
	}

	public int getShareAmount() {
		return shareAmount;
	}

	public int getDailyRewarded() {
		return dailyRewarded;
	}
	
	public void setDailyRewarded(int val) {
		this.dailyRewarded = val;;
	}
	
	public List<Integer> getDayQuestionList() {
		return dayQuestionList;
	}

	public List<Integer> getDayAnswerList() {
		return dayAnswerList;
	}

	public List<Integer> getDayShareList() {
		return dayShareList;
	}

	public List<Integer> getRewardsList() {
		return rewardsList;
	}

	public void addQuestion(int questionId) {
		dayQuestionList.add(questionId);
		dayAnswerList.add(0);
		dayShareList.add(0);
		notifyUpdate();
	}

	public void addAnswer(int answerId) {
		if (dayAnswerList.size() > 0) {
			dayAnswerList.set(dayAnswerList.size() - 1, answerId);
			// 第一次登录发现没有题目，给玩家刷新个题目
			notifyUpdate();
		}
	}

	public void setShare() {
		if (dayShareList.size() > 0) {
			if(dayShareList.get(dayShareList.size() - 1) == 0){
				dayShareList.set(dayShareList.size() - 1, 1);
				shareAmount++;
				notifyUpdate();			
			}
		}
	}

	public void clearDay() {
		dayShareList.clear();
		dayQuestionList.clear();
		dayAnswerList.clear();
		notifyUpdate();
	}

	public void addRewards(int boxId) {
		rewardsList.add(boxId);
		notifyUpdate();
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
	
	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
}
