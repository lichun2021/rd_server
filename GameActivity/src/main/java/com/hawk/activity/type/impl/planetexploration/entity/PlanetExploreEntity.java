package com.hawk.activity.type.impl.planetexploration.entity;

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
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_planet_explore")
public class PlanetExploreEntity extends HawkDBEntity implements IActivityDataEntity{
	
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false)
	private int termId;
    
    //当天首次进入活动的时间
    @IndexProp(id = 4)
	@Column(name="dayTime", nullable = false)
	private long dayTime;
    
    //探索抽奖获得的积分
    @IndexProp(id = 5)
	@Column(name="score", nullable = false)
	private long score;
    
    /** 成就数据 **/
    @IndexProp(id = 6)
	@Column(name="achieveItems", nullable = false)
	private String achieveItems;
    
    @IndexProp(id = 7)
   	@Column(name="collectInfos", nullable = false)
   	private String collectInfos;

    @IndexProp(id = 8)
	@Column(name="createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name="invalid", nullable = false)
	private boolean invalid;
    
    // 今日探索次数
    @IndexProp(id = 11)
	@Column(name="exploreTimes", nullable = false)
	private int exploreTimes;
    
    // 今日采集物品的数量
    @IndexProp(id = 12)
	@Column(name="collectCount", nullable = false)
	private int collectCount;
	
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	@Transient
	private List<PlanetCollectInfo> collectInfoList = new ArrayList<PlanetCollectInfo>();
	
	public PlanetExploreEntity(){}
	
	public PlanetExploreEntity(String playerId, int termId){
		this.playerId = playerId;
		this.termId = termId;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.collectInfos = SerializeHelper.collectionToString(this.collectInfoList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public void afterRead() {
		if (!itemList.isEmpty()) {
			this.itemList.clear();
		}
		if (!collectInfoList.isEmpty()) {
			this.collectInfoList.clear();
		}
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(PlanetCollectInfo.class, this.collectInfos, this.collectInfoList);
	}

	@Override
	public String getPrimaryKey() {
		return id;
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
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	@Override
	public String toString() {
		return "achieveItems:" + achieveItems + ",achieveItemList:" + itemList;
	}

	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}
	
	public void scoreAdd(int scoreAdd) {
		setScore(this.score + scoreAdd);
	}
	
	public void resetItemList(List<AchieveItem> newAchieves) {
		this.itemList.clear();
		this.itemList.addAll(newAchieves);
		this.notifyUpdate();
	}

	public List<PlanetCollectInfo> getCollectInfoList() {
		return collectInfoList;
	}
	
	public int getExploreTimes() {
		return exploreTimes;
	}

	public void setExploreTimes(int exploreTimes) {
		this.exploreTimes = exploreTimes;
	}
	
	public int getCollectCount() {
		return collectCount;
	}

	public void setCollectCount(int collectCount) {
		this.collectCount = collectCount;
	}
}
