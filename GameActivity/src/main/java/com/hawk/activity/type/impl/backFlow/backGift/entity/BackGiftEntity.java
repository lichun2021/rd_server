package com.hawk.activity.type.impl.backFlow.backGift.entity;

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
import com.hawk.activity.type.impl.backFlow.backGift.BackGift;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_back_gift")
public class BackGiftEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	 * 当天抽奖的次数
	 */
    @IndexProp(id = 5)
	@Column(name = "lotteryCount", nullable = false)
	private int lotteryCount;
	
	/**
	 * 当前期抽奖的次数
	 */
    @IndexProp(id = 6)
	@Column(name = "lotteryTotalCount", nullable = false)
	private int lotteryTotalCount;
	
	
	/**
	 * 抽奖时间
	 */
    @IndexProp(id = 7)
	@Column(name = "lotteryTime", nullable = false)
	private long lotteryTime;
	
	/**
	 * 刷新次数
	 */
    @IndexProp(id = 8)
	@Column(name = "refreshCount", nullable = false)
	private int refreshCount;
	
	/**
	 * 上次刷新时间
	 */
    @IndexProp(id = 9)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
	
	/**
	 * 转盘上的奖励
	 */
    @IndexProp(id = 10)
	@Column(name = "awards", nullable = false)
	private String awards;
	
	/**
	 * 上一次奖励的记录
	 */
    @IndexProp(id = 11)
	@Column(name = "awardIndex", nullable = false)
	private int awardIndex;
	
	
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 12)
	@Column(name = "lossDays", nullable = false)
	private int lossDays;
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 13)
	@Column(name = "lossVip", nullable = false)
	private int lossVip;
	
	
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 14)
	@Column(name = "backType", nullable = false)
	private int backType;
	
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
	private List<BackGift> awardList = new CopyOnWriteArrayList<BackGift>();

	
	public BackGiftEntity() {
	}

	public BackGiftEntity(String playerId) {
		this.playerId = playerId;
		this.awards = "";
	}
	
	public BackGiftEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.awards = "";
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

	

	public int getLotteryCount() {
		return lotteryCount;
	}

	public void setLotteryCount(int lotteryCount) {
		this.lotteryCount = lotteryCount;
	}

	public long getLotteryTime() {
		return lotteryTime;
	}

	public void setLotteryTime(long lotteryTime) {
		this.lotteryTime = lotteryTime;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public void setRefreshCount(int refreshCount) {
		this.refreshCount = refreshCount;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	
	
	public int getAwardIndex() {
		return awardIndex;
	}

	public void setAwardIndex(int awardIndex) {
		this.awardIndex = awardIndex;
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
	
	
	
	
	public List<BackGift> getAwardList() {
		return awardList;
	}
	
	
	
	public void resetAwardList(List<BackGift> awardList) {
		this.awardList = awardList;
		this.notifyUpdate();
	}

	@Override
	public void beforeWrite() {
		this.awards = SerializeHelper.collectionToString(this.awardList, SerializeHelper.ELEMENT_DELIMITER);
		
	}
	
	@Override
	public void afterRead() {
		this.awardList.clear();
		SerializeHelper.stringToList(BackGift.class, this.awards, this.awardList);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	
	
	public void addLotteryCount(){
		this.lotteryCount ++;
		this.lotteryTotalCount ++;
	}
	
	public void addRefreshCount(){
		this.refreshCount ++;
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

	public int getLotteryTotalCount() {
		return lotteryTotalCount;
	}

	public void setLotteryTotalCount(int lotteryTotalCount) {
		this.lotteryTotalCount = lotteryTotalCount;
	}
	
	
	
	

	
	
	
	
	
}
