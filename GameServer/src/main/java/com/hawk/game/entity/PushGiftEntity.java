package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import javax.persistence.Transient;

import com.hawk.game.util.MapUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
*	false
*	auto generate do not modified
*/
@Entity
@Table(name="push_gift")
public class PushGiftEntity extends HawkDBEntity{

	/***/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	/**giftId,createTime*/
	@Column(name="giftIdTime", nullable = false, length=512)
    @IndexProp(id = 2)
	private String giftIdTime;

	/**groupId,num*/
	@Column(name="groupRefreshCount", nullable = false, length=256)
    @IndexProp(id = 3)
	private String groupRefreshCount;

	/**重置时间*/
	@Column(name="resetTime", nullable = false, length=19)
    @IndexProp(id = 4)
	private long resetTime;

	/***/
	@Column(name="createTime", nullable = false, length=19)
    @IndexProp(id = 5)
	private long createTime;

	/***/
	@Column(name="updateTime", nullable = false, length=19)
    @IndexProp(id = 6)
	private long updateTime;

	/**false valid true invalid*/
	@Column(name="invalid", nullable = false, length=0)
    @IndexProp(id = 7)
	private boolean invalid;
	
	//添加两个字段记录计数信息、下次清理时间信息
	
	/** groupId,refreshTime */
	@Column(name="groupRefreshTime")
    @IndexProp(id = 8)
	private String groupRefreshTime;
	
	/** groupId,statistics */
	@Column(name="groupStatistics")
    @IndexProp(id = 9)
	private String groupStatistics;
	
	/**
	 * 泰能强化次数
	 */
	@Column(name="plantTechnologyTimes")
    @IndexProp(id = 10)
	private int plantTechnologyTimes;
	
	/**
	 * 泰能战士破译次数
	 */
	@Column(name="plantSoldierCrackTimes")
    @IndexProp(id = 11)
	private int plantSoldierCrackTimes;
	
	/**
	 * 装备强化至x等级次数
	 */
	@Column(name="armourIntensifyTimes")
    @IndexProp(id = 12)
	private String armourIntensifyTimes;
	
	/**
	 * 装备泰晶强化至x等级次数
	 */
	@Column(name="armourStarUpTimes")
    @IndexProp(id = 13)
	private String armourStarUpTimes;

	/** complex type @giftIdTime*/
	@Transient
	private Map<Integer, Integer> giftIdTimeMap;

	/** complex type @groupRefreshCount*/
	@Transient
	private Map<Integer, Integer> groupRefreshCountMap;
	
	/** complex type @groupRefreshTime*/
	@Transient
	private Map<Integer, Integer> groupRefreshTimeMap;
	
	/** complex type @groupStatistics*/
	@Transient
	private Map<Integer, Integer> groupStatisticsMap;
	
	/** complex type @armourIntensifyTimes*/
	@Transient
	private Map<Integer, Integer> armourIntensifyTimesMap;
	
	/** complex type @armourStarUpTimes*/
	@Transient
	private Map<Integer, Integer> armourStarUpTimesMap;
	
	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getGiftIdTime() {
		return this.giftIdTime; 
	}

	public void setGiftIdTime(String giftIdTime) {
		this.giftIdTime = giftIdTime;
	}

	public String getGroupRefreshCount() {
		return this.groupRefreshCount; 
	}

	public void setGroupRefreshCount(String groupRefreshCount) {
		this.groupRefreshCount = groupRefreshCount;
	}

	public long getResetTime() {
		return this.resetTime; 
	}

	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
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

	public Map<Integer, Integer> getGiftIdTimeMap() {
		return this.giftIdTimeMap; 
	}

	public void setGiftIdTimeMap(Map<Integer, Integer> giftIdTimeMap) {
		this.giftIdTimeMap = giftIdTimeMap;
	}

	public Map<Integer, Integer> getGroupRefreshCountMap() {
		return this.groupRefreshCountMap; 
	}

	public void setGroupRefreshCountMap(Map<Integer, Integer> groupRefreshCountMap) {
		this.groupRefreshCountMap = groupRefreshCountMap;
	}

	@Override
	public void afterRead() {		
		this.giftIdTimeMap = SerializeHelper.stringToMap(giftIdTime, Integer.class, Integer.class);
		this.groupRefreshCountMap = SerializeHelper.stringToMap(groupRefreshCount, Integer.class, Integer.class);
		this.groupRefreshTimeMap = SerializeHelper.stringToMap(groupRefreshTime, Integer.class, Integer.class);
		this.groupStatisticsMap = SerializeHelper.stringToMap(groupStatistics, Integer.class, Integer.class);
		this.armourIntensifyTimesMap = SerializeHelper.stringToMap(armourIntensifyTimes, Integer.class, Integer.class);
		this.armourStarUpTimesMap = SerializeHelper.stringToMap(armourStarUpTimes, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {		
		this.giftIdTime = SerializeHelper.mapToString(giftIdTimeMap);
		this.groupRefreshCount = SerializeHelper.mapToString(groupRefreshCountMap);
		this.groupRefreshTime = SerializeHelper.mapToString(groupRefreshTimeMap);
		this.groupStatistics = SerializeHelper.mapToString(groupStatisticsMap);
		this.armourIntensifyTimes = SerializeHelper.mapToString(armourIntensifyTimesMap);
		this.armourStarUpTimes = SerializeHelper.mapToString(armourStarUpTimesMap);
	}

	public void addGiftIdTime(Integer key, Integer value) {
		this.giftIdTimeMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeGiftIdTime(Integer key) {
		this.giftIdTimeMap.remove(key);
		this.notifyUpdate();
	}
	public void addGroupRefreshCount(Integer key, Integer value) {
		this.groupRefreshCountMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeGroupRefreshCount(Integer key) {
		this.groupRefreshCountMap.remove(key);
		this.notifyUpdate();
	}
	
	public Map<Integer, Integer> getGroupRefreshTimeMap() {
		return groupRefreshTimeMap;
	}
	
	public Map<Integer, Integer> getGroupStatisticsMap() {
		return groupStatisticsMap;
	}
	
	public void addStatistics(Integer groupId, Integer addValue) {
		MapUtil.appendIntValue(groupStatisticsMap, groupId, addValue);
		this.notifyUpdate();
	}
	
	public void removeStatistics(Integer groupId) {
		groupStatisticsMap.remove(groupId);
		this.notifyUpdate();
	}
	
	public int getStatistics(Integer groupId) {
		return MapUtil.getIntValue(groupStatisticsMap, groupId);
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("push gift entity primaryKey is playerId");	
	}
	
	public String getOwnerKey() {
		return playerId;
	}

	public int getPlantTechnologyTimes() {
		return plantTechnologyTimes;
	}

	/**
	 * 增加泰能强化统计次数
	 */
	public void addPlantTechnologyTimes() {
		this.plantTechnologyTimes++;
		notifyUpdate();
	}

	public int getPlantSoldierCrackTimes() {
		return plantSoldierCrackTimes;
	}

	/**
	 * 泰能战士破译统计次数
	 */
	public void addPlantSoldierCrackTimes() {
		this.plantSoldierCrackTimes++;
		notifyUpdate();
	}

	public int getArmourIntensifyTimes(int level) {
		return armourIntensifyTimesMap.getOrDefault(level, 0);
	}

	/**
	 * 增加装备强化统计次数
	 * @param level
	 */
	public int addArmourIntensifyTimes(int level) {
		MapUtil.appendIntValue(armourIntensifyTimesMap, level, 1);
		notifyUpdate();
		return armourIntensifyTimesMap.get(level);
	}

	public int getArmourStarUpTimesMap(int level) {
		return armourStarUpTimesMap.getOrDefault(level, 0);
	}

	/**
	 * 增加装备泰能强化统计次数
	 * @param level
	 */
	public int addArmourStarUpTimesMap(int level) {
		MapUtil.appendIntValue(armourStarUpTimesMap, level, 1);
		notifyUpdate();
		return armourStarUpTimesMap.get(level);
	}
}
