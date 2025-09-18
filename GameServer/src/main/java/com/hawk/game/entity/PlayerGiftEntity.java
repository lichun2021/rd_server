package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import javax.persistence.Transient;

import com.hawk.game.entity.item.GiftAdviceItem;
import com.hawk.game.util.MapUtil;
import com.hawk.serialize.string.SerializeHelper;
import java.util.List;

/**
*	超值礼包
*	auto generate do not modified
*/
@Entity
@Table(name="player_gift")
public class PlayerGiftEntity extends HawkDBEntity{

	/***/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	/**gift group id list*/
	@Column(name="giftGroupIds", length = 1024)
    @IndexProp(id = 2)
	private String giftGroupIds;

	/**{poolId, lastResetTime}*/
	@Column(name="poolResetTimes", length = 1024)
    @IndexProp(id = 3)
	private String poolResetTimes;

	/**rootGroupId,giftGroupId*/
	@Column(name="rootGroupIdRefRecords", length = 1024)
    @IndexProp(id = 4)
	private String rootGroupIdRefRecords;

	/** 周期次数*/
	@Column(name="lastRefreshTime", nullable = false)
    @IndexProp(id = 5)
	private long lastRefreshTime;

	/***/
	@Column(name="resetTime")
    @IndexProp(id = 6)
	private long resetTime;

	/**{groupId,level*/
	@Column(name="buyLevels", length = 1024)
    @IndexProp(id = 7)
	private String buyLevels;

	/**{giftId,num}*/
	@Column(name="buyNums", nullable = false, length = 1024)
    @IndexProp(id = 8)
	private String buyNums;

	/***/
	@Column(name="createTime", nullable = false )
    @IndexProp(id = 9)
	private long createTime;

	/***/
	@Column(name="updateTime", nullable = false)
    @IndexProp(id = 10)
	private long updateTime;

	/***/
	@Column(name="invalid")
    @IndexProp(id = 11)
	private boolean invalid;

	/**
	 * 礼包推荐
	 */
	@Column(name="giftAdvice")
    @IndexProp(id = 12)
	private String giftAdvice;
	
	/**
	 * 按周刷新的礼包的重置时间
	 */
	@Column(name="weekResetTime")
    @IndexProp(id = 13)
	private long weekResetTime;
	
	
	/** complex type @giftGroupIds*/
	@Transient
	private List<Integer> giftGroupIdsList;

	/** complex type @poolResetTimes*/
	@Transient
	private Map<Integer, Integer> poolResetTimesMap;

	/** complex type @rootGroupIdRefRecords*/
	@Transient
	private Map<Integer, Integer> rootGroupIdRefRecordsMap;

	/** complex type @buyLevels*/
	@Transient
	private Map<Integer, Integer> buyLevelsMap;

	/** complex type @buyNums*/
	@Transient
	private Map<Integer, Integer> buyNumsMap;

	/**
	 * 推荐礼包集合
	 */
	@Transient
	private Map<Integer, GiftAdviceItem> giftAdviceMap;
	
	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getGiftGroupIds() {
		return this.giftGroupIds; 
	}

	public void setGiftGroupIds(String giftGroupIds) {
		this.giftGroupIds = giftGroupIds;
	}

	public String getPoolResetTimes() {
		return this.poolResetTimes; 
	}

	public void setPoolResetTimes(String poolResetTimes) {
		this.poolResetTimes = poolResetTimes;
	}

	public String getRootGroupIdRefRecords() {
		return this.rootGroupIdRefRecords; 
	}

	public void setRootGroupIdRefRecords(String rootGroupIdRefRecords) {
		this.rootGroupIdRefRecords = rootGroupIdRefRecords;
	}

	public long getLastRefreshTime() {
		return this.lastRefreshTime; 
	}

	public void setLastRefreshTime(long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public long getResetTime() {
		return this.resetTime; 
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}

	public String getBuyLevels() {
		return this.buyLevels; 
	}

	public void setBuyLevels(String buyLevels) {
		this.buyLevels = buyLevels;
	}

	public String getBuyNums() {
		return this.buyNums; 
	}

	public void setBuyNums(String buyNums) {
		this.buyNums = buyNums;
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

	public List<Integer> getGiftGroupIdsList() {
		return this.giftGroupIdsList; 
	}

	public void setGiftGroupIdsList(List<Integer> giftGroupIdsList) {
		this.giftGroupIdsList = giftGroupIdsList;
	}

	public Map<Integer, Integer> getPoolResetTimesMap() {
		return this.poolResetTimesMap; 
	}

	public void setPoolResetTimesMap(Map<Integer, Integer> poolResetTimesMap) {
		this.poolResetTimesMap = poolResetTimesMap;
	}

	public Map<Integer, Integer> getRootGroupIdRefRecordsMap() {
		return this.rootGroupIdRefRecordsMap; 
	}

	public void setRootGroupIdRefRecordsMap(Map<Integer, Integer> rootGroupIdRefRecordsMap) {
		this.rootGroupIdRefRecordsMap = rootGroupIdRefRecordsMap;
	}

	public Map<Integer, Integer> getBuyLevelsMap() {
		return this.buyLevelsMap; 
	}

	public void setBuyLevelsMap(Map<Integer, Integer> buyLevelsMap) {
		this.buyLevelsMap = buyLevelsMap;
	}

	public Map<Integer, Integer> getBuyNumsMap() {
		return this.buyNumsMap; 
	}

	public void setBuyNumsMap(Map<Integer, Integer> buyNumsMap) {
		this.buyNumsMap = buyNumsMap;
	}

	@Override
	public void afterRead() {		
		this.giftGroupIdsList = SerializeHelper.stringToList(Integer.class, giftGroupIds, SerializeHelper.ATTRIBUTE_SPLIT);
		this.poolResetTimesMap = SerializeHelper.stringToMap(poolResetTimes, Integer.class, Integer.class);
		this.rootGroupIdRefRecordsMap = SerializeHelper.stringToMap(rootGroupIdRefRecords, Integer.class, Integer.class);
		this.buyLevelsMap = SerializeHelper.stringToMap(buyLevels, Integer.class, Integer.class);
		this.buyNumsMap = SerializeHelper.stringToMap(buyNums, Integer.class, Integer.class);
		this.giftAdviceMap = SerializeHelper.stringToMap(giftAdvice, Integer.class, GiftAdviceItem.class);
	}

	@Override
	public void beforeWrite() {		
		this.giftGroupIds = SerializeHelper.collectionToString(giftGroupIdsList, SerializeHelper.ATTRIBUTE_SPLIT);
		this.poolResetTimes = SerializeHelper.mapToString(poolResetTimesMap);
		this.rootGroupIdRefRecords = SerializeHelper.mapToString(rootGroupIdRefRecordsMap);
		this.buyLevels = SerializeHelper.mapToString(buyLevelsMap);
		this.buyNums = SerializeHelper.mapToString(buyNumsMap);
		this.giftAdvice = SerializeHelper.mapToString(giftAdviceMap);
	}
	
	public void addBuyLevel(int groupId, int level) {
		this.buyLevelsMap.put(groupId, level);
		this.notifyUpdate();		
	}

	public void removeBuyLevel(Integer removeId) {
		this.buyLevelsMap.remove(removeId);
		this.notifyUpdate();
	}
	
	public void addGiftGroupIds(List<Integer> idList) {
		this.giftGroupIdsList.addAll(idList);
		this.notifyUpdate();
	}
	
	public void addGiftGroupId(Integer id) {
		giftGroupIdsList.add(id);
		this.notifyUpdate();
	}

	public void removeGiftGroupId(Integer id) {
		this.giftGroupIdsList.remove(id);
		this.notifyUpdate();
	}

	public int getBuyLevel(Integer id) {
		return MapUtil.getIntValue(this.buyLevelsMap, id);
	}

	public int getBuyNum(int id) {
		return MapUtil.getIntValue(this.buyNumsMap, id);
	}

	public void removeBuyNum(Integer removeId) {
		this.buyNumsMap.remove(removeId);
		this.notifyUpdate();
	}

	public void addBuyNum(int id, int i) {
		MapUtil.appendIntValue(this.buyNumsMap, id, i);
		this.notifyUpdate();
	}
	
	public void addPoolResetTimeIfAbsent(Integer rootGroupId, Integer refId) {
		this.poolResetTimesMap.putIfAbsent(rootGroupId, refId);
		this.notifyUpdate();
	}
	
	public void addRootGroupIdRef(Integer rootGroupId, Integer refId) {
		this.rootGroupIdRefRecordsMap.put(rootGroupId, refId);
		this.notifyUpdate();
	}
	
	public Integer getRootGroupIdRef(Integer rootGroupId) {
		return this.rootGroupIdRefRecordsMap.get(rootGroupId);
	}

	public void removeGiftGroupIds(List<Integer> removeIds) {
		this.giftGroupIdsList.removeAll(removeIds);
		this.notifyUpdate();
	}

	public GiftAdviceItem getGiftAdviceInfo(int giftGroupId) {
		return giftAdviceMap.get(giftGroupId);
	}

	public void addGiftAdviceInfo(GiftAdviceItem giftAdviceInfo) {
		giftAdviceMap.put(giftAdviceInfo.getGiftGroupId(), giftAdviceInfo);
		notifyUpdate();
	}
	
	public void removeGiftAdvice(int giftGroupId) {
		giftAdviceMap.remove(giftGroupId);
		notifyUpdate();
	}
	
	public void removeGiftAdvices(List<Integer> removeIds) {
		for (int giftGroupId : removeIds) {
			giftAdviceMap.remove(giftGroupId);
		}
		this.notifyUpdate();
	}
	
	public void clearDailyGiftAdvice() {
		for (GiftAdviceItem gift : giftAdviceMap.values()) {
			gift.clearDayAdvice();
		}
		this.notifyUpdate();
	}
	
	public void resetGiftAdviceCd() {
		for (GiftAdviceItem gift : giftAdviceMap.values()) {
			gift.setLastAdviceTime(HawkTime.getMillisecond());
		}
		this.notifyUpdate();
	}
	
	public long getWeekResetTime() {
		return weekResetTime;
	}

	public void setWeekResetTime(long weekResetTime) {
		this.weekResetTime = weekResetTime;
	}
	
	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("player gift entity primaryKey is playerId");		
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
