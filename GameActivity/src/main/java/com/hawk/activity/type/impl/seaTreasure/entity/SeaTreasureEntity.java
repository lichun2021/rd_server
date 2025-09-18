package com.hawk.activity.type.impl.seaTreasure.entity;

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

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.seaTreasure.item.SeaTreasureBoxItem;
import com.hawk.activity.type.impl.seaTreasure.item.SeaTreasureReceiveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
@Entity
@Table(name="activity_sea_treasure")
public class SeaTreasureEntity extends AchieveActivityEntity implements IActivityDataEntity{
	
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
	 * 每日标记
	 */
    @IndexProp(id = 4)
	@Column(name = "dayMark", nullable = false)
	private int dayMark;
	
	/**
	 * 寻宝次数
	 */
    @IndexProp(id = 5)
	@Column(name = "findTimes", nullable = false)
	private int findTimes;
	
	/**
	 * 加速道具购买次数
	 */
    @IndexProp(id = 6)
	@Column(name = "toolBuyTimes", nullable = false)
	private int toolBuyTimes;
	
	/**
	 * 奖励接受次数记录
	 */
    @IndexProp(id = 7)
	@Column(name = "receiveTimes", nullable = false)
	private String receiveTimes;
	
	/**
	 * 宝箱信息
	 */
    @IndexProp(id = 8)
	@Column(name = "boxInfos", nullable = false)
	private String boxInfos;
	
	/**
	 * 已接受的普通奖励
	 */
    @IndexProp(id = 9)
	@Column(name = "receiveRewards", nullable = false)
	private String receiveRewards;
	
	/**
	 * 已接受的额外奖励信息
	 */
    @IndexProp(id = 10)
	@Column(name = "receiveAdvRewards", nullable = false)
	private String receiveAdvRewards;
	
    @IndexProp(id = 11)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 12)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 13)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	/**
	 * 奖励接受次数
	 */
	@Transient
	private Map<Integer, Integer> receiveTimesMap = new HashMap<>();
	
	/**
	 * 宝箱信息
	 */
	@Transient
	private Map<Integer, SeaTreasureBoxItem> boxInfoMap = new HashMap<>();
	
	/**
	 * 普通奖励获取信息
	 */
	@Transient
	private Map<Integer, Integer> commonReceiveInfoMap = new HashMap<>();
	
	/**
	 * 高级奖励获取信息
	 */
	@Transient
	private List<SeaTreasureReceiveItem> advReceiveInfosList = new ArrayList<>();
	
	public SeaTreasureEntity() {
		
	}
	
	public SeaTreasureEntity(String playerId, int termId) {
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

	public int getDayMark() {
		return dayMark;
	}

	public void setDayMark(int dayMark) {
		this.dayMark = dayMark;
	}

	public int getFindTimes() {
		return findTimes;
	}

	public void addFindTimes() {
		this.findTimes++;
		notifyUpdate();
	}

	public int getToolBuyTimes() {
		return toolBuyTimes;
	}

	public void addToolBuyTimes(int addTimes) {
		this.toolBuyTimes += addTimes;
		notifyUpdate();
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
		return this.invalid;
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
	
	@Override
	public void beforeWrite() {
		receiveTimes = SerializeHelper.mapToString(receiveTimesMap);
		boxInfos = SerializeHelper.mapToString(boxInfoMap);
		receiveRewards = SerializeHelper.mapToString(commonReceiveInfoMap);
		receiveAdvRewards = SerializeHelper.collectionToString(advReceiveInfosList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		receiveTimesMap = SerializeHelper.stringToMap(receiveTimes);
		boxInfoMap = SerializeHelper.stringToMap(boxInfos, Integer.class, SeaTreasureBoxItem.class);
		commonReceiveInfoMap = SerializeHelper.stringToMap(receiveRewards);
		this.advReceiveInfosList.clear();
		SerializeHelper.stringToList(SeaTreasureReceiveItem.class, receiveAdvRewards, advReceiveInfosList);
	}

	public Map<Integer, Integer> getReceiveTimesMap() {
		return receiveTimesMap;
	}

	public int getReceiveTimes(int id) {
		return receiveTimesMap.getOrDefault(id, 0);
	}
	
	public void addReceiveTimes(int id) {
		int beforeTimes = receiveTimesMap.getOrDefault(id, 0);
		receiveTimesMap.put(id, beforeTimes + 1);
		notifyUpdate();
	}
	
	public Map<Integer, SeaTreasureBoxItem> getBoxInfoMap() {
		return boxInfoMap;
	}

	public SeaTreasureBoxItem getBoxInfo(int grid) {
		return boxInfoMap.get(grid);
	}
	
	public void addBoxInfo(SeaTreasureBoxItem item) {
		boxInfoMap.put(item.getGrid(), item);
		notifyUpdate();
	}
	
	public void removeBoxInfo(int grid) {
		boxInfoMap.remove(grid);
		notifyUpdate();
	}
	
	public void clearBoxInfo() {
		boxInfoMap.clear();
		notifyUpdate();
	}
	
	public Map<Integer, Integer> getCommonReceiveInfos() {
		return commonReceiveInfoMap;
	}

	public void addCommonReceiveInfos(int id) {
		int beforeTimes = commonReceiveInfoMap.getOrDefault(id, 0);
		commonReceiveInfoMap.put(id, beforeTimes + 1);
		notifyUpdate();
	}
	
	public List<SeaTreasureReceiveItem> getAdvReceiveInfosList() {
		return advReceiveInfosList;
	}
	
	public void addAdvReceiveInfo(SeaTreasureReceiveItem info) {
		advReceiveInfosList.add(info);
		notifyUpdate();
	}
	
	public void dailyClear() {
		findTimes = 0;
		receiveTimesMap.clear();
		commonReceiveInfoMap.clear();
		advReceiveInfosList.clear();
		notifyUpdate();
	}
}
