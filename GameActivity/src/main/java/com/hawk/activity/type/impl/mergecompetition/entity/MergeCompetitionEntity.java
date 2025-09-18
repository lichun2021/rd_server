package com.hawk.activity.type.impl.mergecompetition.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * 新服合服比拼活动
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_merge_competition")
public class MergeCompetitionEntity extends HawkDBEntity implements IActivityDataEntity {

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

    /** 成就数据 **/
    @IndexProp(id = 4)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems;
    
    /** 未领取的嘉奖礼包 */
    @IndexProp(id = 5)
   	@Column(name="awardIds", nullable = false)
   	private String awardIds;
    
    /** 更新联盟战力成就数据的时间 */
    @IndexProp(id = 6)
   	@Column(name="guildPowerTargetTime", nullable = false)
    private volatile long guildPowerTargetTime;
    
    /** 嘉奖礼包数据更新的时间 */
    @IndexProp(id = 7)
   	@Column(name="awardIdRefreshTime", nullable = false)
    private volatile long awardIdRefreshTime;
    
    /** 消耗体力 */
    @IndexProp(id = 8)
   	@Column(name="costVit", nullable = false)
    private int costVit;
    
    /** 嘉奖积分 */
    @IndexProp(id = 9)
   	@Column(name="giftScore", nullable = false)
    private int giftScore;
	
	@IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 11)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 12)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    /** 联盟去兵战力目标全部达成 */
    @IndexProp(id = 13)
   	@Column(name="guildPowerTargetFinish", nullable = false)
    private int guildPowerTargetFinish;

    
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
    
    @Transient
   	private Map<Integer, Integer> awardMap = new HashMap<>();
    
    @Transient
    private Object giftRewardLocker = new Object();
    @Transient
    private Object guildPowerLocker = new Object();
	
	
	public MergeCompetitionEntity() {
	}

	public MergeCompetitionEntity(String playerId) {
		this.playerId = playerId;
	}

	public MergeCompetitionEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.awardIds = SerializeHelper.mapToString(awardMap, "_", ",");
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		this.awardMap = SerializeHelper.stringToMap(awardIds, Integer.class, Integer.class, "_", ",");
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
	}
	
	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}
	
	public long getGuildPowerTargetTime() {
		synchronized(guildPowerLocker) {
			return guildPowerTargetTime;
		}
	}

	public void setGuildPowerTargetTime(long guildPowerTargetTime) {
		synchronized(guildPowerLocker) {
			this.guildPowerTargetTime = guildPowerTargetTime;
		}
	}
	
	public long getAwardIdRefreshTime() {
		synchronized(giftRewardLocker) {
			return awardIdRefreshTime;
		}
	}

	public void setAwardIdRefreshTime(long awardIdRefreshTime) {
		synchronized(giftRewardLocker) {
			this.awardIdRefreshTime = awardIdRefreshTime;
		}
	}

	public Map<Integer, Integer> getAwardMap() {
		return awardMap;
	}
	
	public void addAwardId(int awardId, int count) {
		count += awardMap.getOrDefault(awardId, 0);
		awardMap.put(awardId, count);
		this.notifyUpdate();
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

	public int getCostVit() {
		return costVit;
	}

	public void setCostVit(int costVit) {
		this.costVit = costVit;
	}
	
	public int getGiftScore() {
		return giftScore;
	}

	public void setGiftScore(int giftScore) {
		this.giftScore = giftScore;
	}
	
	public int getGuildPowerTargetFinish() {
		return guildPowerTargetFinish;
	}

	public void setGuildPowerTargetFinish(int guildPowerTargetFinish) {
		this.guildPowerTargetFinish = guildPowerTargetFinish;
	}
}
