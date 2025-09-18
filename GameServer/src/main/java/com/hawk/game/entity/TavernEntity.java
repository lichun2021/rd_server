package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 酒馆
 * @author PhilChen
 *
 */
@Entity
@Table(name = "tavern")
public class TavernEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = "";

	/** 最后一次刷新时间*/
	@Column(name = "lastRefreshTime", nullable = false)
    @IndexProp(id = 2)
	private long lastRefreshTime;
	
	/** 积分成就项数据 */
	@Column(name = "scoreAchieveItems", nullable = false)
    @IndexProp(id = 3)
	private String scoreAchieveItems;

	/** 成就项数据 */
	@Column(name = "achieveItems", nullable = false)
    @IndexProp(id = 4)
	private String achieveItems;

	/** 成就项完成次数 achieveId:次数 */
	@Column(name = "achieveFinishCount", nullable = false)
    @IndexProp(id = 5)
	private String achieveFinishCount;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	protected boolean invalid;

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<>();
	
	@Transient
	private List<AchieveItem> scoreItemList = new CopyOnWriteArrayList<>();
	
	@Transient
	private Map<Integer, Integer> achieveFinishMap = new ConcurrentHashMap<>();

	public TavernEntity() {
		
	}

	public TavernEntity(String playerId, long lastRefreshTime) {
		this.playerId = playerId;
		this.lastRefreshTime = lastRefreshTime;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public List<AchieveItem> getAchieveItemList() {
		return itemList;
	}

	public List<AchieveItem> getScoreItemList() {
		return scoreItemList;
	}

	public Map<Integer, Integer> getAchieveFinishMap() {
		return achieveFinishMap;
	}
	
	public int getFinishCount(int achieveId) {
		Integer finishCount = achieveFinishMap.get(achieveId);
		if (finishCount == null) {
			return 0;
		}
		return finishCount;
	}
	
	public void addScoreItem(AchieveItem item) {
		scoreItemList.add(item);
	}
	
	public long getLastRefreshTime() {
		return lastRefreshTime;
	}
	
	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	
	public int addFinishCount(int achieveId, int count) {
		Integer oldCount = achieveFinishMap.get(achieveId);
		if (oldCount == null) {
			oldCount = 0;
		}
		oldCount += count;
		achieveFinishMap.put(achieveId, oldCount);
		notifyUpdate();
		return oldCount;
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
		this.achieveItems = SerializeHelper.collectionToString(this.itemList);
		this.scoreAchieveItems = SerializeHelper.collectionToString(this.scoreItemList);
		this.achieveFinishCount = SerializeHelper.mapToString(this.achieveFinishMap);
	}

	@Override
	public void afterRead() {
		this.itemList = new CopyOnWriteArrayList<>();
		this.scoreItemList = new CopyOnWriteArrayList<>();
		this.achieveFinishMap = new ConcurrentHashMap<>();
		
		this.itemList.clear();
		this.scoreItemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToList(AchieveItem.class, this.scoreAchieveItems, this.scoreItemList);
		SerializeHelper.stringToMap(this.achieveFinishCount, Integer.class, Integer.class, this.achieveFinishMap);
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		playerId = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
