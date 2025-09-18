package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.entity.item.WishingCountItem;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 许愿池
 * @author PhilChen
 *
 */
@Entity
@Table(name = "wishing_well")
public class WishingWellEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = "";
	
	/** 最后一次许愿时间*/
	@Column(name = "lastWishTime", nullable = false)
    @IndexProp(id = 2)
	private long lastWishTime;

	/** 今日各个资源已许愿的次数*/
	@Column(name = "todayWishCounts", nullable = false)
    @IndexProp(id = 3)
	private String todayWishCounts;

	/** 额外可许愿次数*/
	@Column(name = "extraWishCount", nullable = false)
    @IndexProp(id = 4)
	private int extraWishCount;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 5)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 6)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 7)
	protected boolean invalid;
	
	@Transient
	private Map<Integer, WishingCountItem> todayWishCountMap = new ConcurrentHashMap<>();
	
	public WishingWellEntity() {
	}
	
	public WishingWellEntity(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public long getLastWishTime() {
		return lastWishTime;
	}

	public void setLastWishTime(long lastWishTime) {
		this.lastWishTime = lastWishTime;
	}

	public int getExtraWishCount() {
		return extraWishCount;
	}

	public void setExtraWishCount(int extraWishCount) {
		this.extraWishCount = extraWishCount;
	}

	public int getTodayTotalWishCount() {
		int count = 0;
		for (WishingCountItem item : todayWishCountMap.values()) {
			count += item.getFreeCount() + item.getCostCount();
		}
		return count;
	}

	public int getTodayFreeWishCount() {
		int count = 0;
		for (WishingCountItem item : todayWishCountMap.values()) {
			count += item.getFreeCount();
		}
		return count;
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
		todayWishCounts = SerializeHelper.collectionToString(todayWishCountMap.values(), SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		todayWishCountMap = new ConcurrentHashMap<>();
		List<WishingCountItem> list = SerializeHelper.stringToList(WishingCountItem.class, this.todayWishCounts);
		for (WishingCountItem item : list) {
			todayWishCountMap.put(item.getResourceType(), item);
		}
	}
	
	public int getFreeCount(PlayerAttr resourceType) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			return 0;
		}
		return item.getFreeCount();
	}
	
	public int getCostCount(PlayerAttr resourceType) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			return 0;
		}
		return item.getCostCount();
	}

	public int getExtraCount(PlayerAttr resourceType) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			return 0;
		}
		return item.getExtraCount();
	}
	
	public void addFreeCount(PlayerAttr resourceType, int count) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			item = new WishingCountItem(resourceType.getNumber());
			todayWishCountMap.put(resourceType.getNumber(), item);
		}
		item.addFreeCount(count);
		notifyUpdate();
	}

	public void addCostCount(PlayerAttr resourceType, int count) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			item = new WishingCountItem(resourceType.getNumber());
			todayWishCountMap.put(resourceType.getNumber(), item);
		}
		item.addCostCount(count);
		notifyUpdate();
	}

	public void addExtraCount(PlayerAttr resourceType, int count) {
		WishingCountItem item = todayWishCountMap.get(resourceType.getNumber());
		if (item == null) {
			item = new WishingCountItem(resourceType.getNumber());
			todayWishCountMap.put(resourceType.getNumber(), item);
		}
		item.addExtraCount(count);
		notifyUpdate();
	}
	
	public void cleanCount() {
		todayWishCountMap.clear();
		notifyUpdate();
	}
	
	public Map<Integer, WishingCountItem> getTodayWishCountMap() {
		return todayWishCountMap;
	}

	public int getWishingCount(PlayerAttr resourceType) {
		int freeCount = getFreeCount(resourceType);
		int costCount = getCostCount(resourceType);
		int extraCount = getExtraCount(resourceType);
		return freeCount + costCount + extraCount;
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
