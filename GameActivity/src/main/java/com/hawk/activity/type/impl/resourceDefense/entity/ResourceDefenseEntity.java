package com.hawk.activity.type.impl.resourceDefense.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.resourceDefense.temp.ResourceDefenseTemplate;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 资源保卫战
 * @author golden
 *
 */
@Entity
@Table(name = "activity_resource_defense")
public class ResourceDefenseEntity extends HawkDBEntity implements IActivityDataEntity {

	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;
	
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=50)
	private String playerId;

    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/**
	 * 资源站经验
	 */
    @IndexProp(id = 4)
	@Column(name="exp", nullable = false, length=10)
	private int exp;
	
	/**
	 * 资源站信息
	 */
    @IndexProp(id = 5)
	@Column(name = "stationInfo", nullable = false)
	private String stationInfo;
		
	/**
	 * 是否解锁高级奖励
	 */
    @IndexProp(id = 6)
	@Column(name="unlcokSuper", nullable = false, length=10)
	private int unlcokSuper;
	
	/**
	 * 已接受的奖励id
	 */
    @IndexProp(id = 7)
	@Column(name = "receivedRewardId", nullable = false)
	private String receivedRewardId;
	
	/**
	 * 偷取次数
	 */
    @IndexProp(id = 8)
	@Column(name="stealTimes", nullable = false, length=10)
	private int stealTimes;
	
	/**
	 * 被偷取次数
	 */
    @IndexProp(id = 9)
	@Column(name="beStealTimes", nullable = false, length=10)
	private int beStealTimes;
	
	/**
	 * 购买经验次数
	 */
    @IndexProp(id = 10)
	@Column(name="buyExpInfo", nullable = false)
	private String buyExpInfo;
	
	/**
	 * 购买经验次数刷新时间
	 */
    @IndexProp(id = 11)
	@Column(name="buyExpRefreshTime", nullable = false)
	private long buyExpRefreshTime;

    @IndexProp(id = 12)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 13)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 14)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	/**
	 * 可偷取次数
	 */
    @IndexProp(id = 15)
	@Column(name="canStealTimes", nullable = false, length=10)
	private int canStealTimes;
	
	/**
	 * 偷取次数tick时间
	 */
    @IndexProp(id = 16)
	@Column(name = "stealTimesTick", nullable = false)
	private long stealTimesTick;
	
	/**
	 * 偷取次数零点tick时间
	 */
    @IndexProp(id = 17)
	@Column(name = "stealTimesZeroTick", nullable = false)
	private long stealTimesZeroTick;
    //新增/////////
	/**
	 * 特工技能信息
	 */
    @IndexProp(id = 18)
	@Column(name="agentSkill", nullable = false)
	private String agentSkill;

	/**
	 * 特工履历信息
	 */
    @IndexProp(id = 19)
	@Column(name="agentRecord", nullable = false)
	private String agentRecord;
	/**
	 * 是否激活技能 跨天清
	 */
    @IndexProp(id = 20)
	@Column(name="activeSkill", nullable = false, length=10)
	private int activeSkill;
	/**
	 * 特工技能已经刷新的次数 跨天清
	 */
    @IndexProp(id = 21)
	@Column(name="skillRefreshTimes", nullable = false, length=10)
	private int skillRefreshTimes;

	/**
	 * 特工技能免费次数 跨天重置
	 */
    @IndexProp(id = 22)
	@Column(name="freeRefreshTimes", nullable = false, length=10)
	private int freeRefreshTimes;
	/**
	 * 高级机器人资源站有个缓存数据,此字段存储
	 * 特工技能召唤高级机器人数据 robotId_stealTimes ,同时判断高级机器人是否激活,,跨天清
	 */
    @IndexProp(id = 23)
	@Column(name="greatRobotInfo", nullable = false)
	private String greatRobotInfo;

	/**
	 * 玩家偷取机器人每日的次数,(2和3类型有限制) 跨天清
	 * robotType_num
	 */
    @IndexProp(id = 24)
	@Column(name="stealRobotInfo", nullable = false)
	private String stealRobotInfo;


	@Transient
	private List<ResourceDefenseTemplate> stationInfoList = new ArrayList<>();
	
	@Transient
	private Set<Integer> receivedRewardIdSet = new HashSet<>();
	
	@Transient
	private Map<Integer, Integer> buyExpInfoMap = new HashMap<>();

	/**
	 * key:id  value:特工技能已经使用次数
	 */
	@Transient
	private Map<Integer, Integer> agentSkillInfoMap = new HashMap<>();


	@Transient
	private List<Long> agentRecordList = new ArrayList<>();

	@Transient
	private Map<Integer, Integer> greatRobotInfoMap = new HashMap<>();

	@Transient
	private Map<Integer, Integer> stealRobotInfoMap = new HashMap<>();

	@Transient
	private long cdTime;

	@Override
	public void beforeWrite() {
		stationInfo = SerializeHelper.collectionToString(stationInfoList, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.ATTRIBUTE_SPLIT);
		receivedRewardId = SerializeHelper.collectionToString(receivedRewardIdSet, SerializeHelper.ELEMENT_DELIMITER);
		buyExpInfo = SerializeHelper.mapToString(buyExpInfoMap);
		agentSkill = SerializeHelper.mapToString(agentSkillInfoMap);
		agentRecord = SerializeHelper.collectionToString(agentRecordList);
		greatRobotInfo = SerializeHelper.mapToString(greatRobotInfoMap);
		stealRobotInfo = SerializeHelper.mapToString(stealRobotInfoMap);
	}

	@Override
	public void afterRead() {
		
		if (!HawkOSOperator.isEmptyString(stationInfo)) {
			stationInfoList = SerializeHelper.stringToList(ResourceDefenseTemplate.class, stationInfo);
		}
		
		if (!HawkOSOperator.isEmptyString(receivedRewardId)) {
			receivedRewardIdSet = SerializeHelper.stringToSet(Integer.class, receivedRewardId, SerializeHelper.ELEMENT_SPLIT);
		}
		
		if (!HawkOSOperator.isEmptyString(buyExpInfo)) {
			buyExpInfoMap = SerializeHelper.stringToMap(buyExpInfo);
		}

		if (!HawkOSOperator.isEmptyString(agentSkill)) {
			agentSkillInfoMap = SerializeHelper.stringToMap(agentSkill);
		}
		if (!HawkOSOperator.isEmptyString(agentRecord)) {
			agentRecordList = SerializeHelper.stringToList(Long.class, agentRecord);
		}
		if (!HawkOSOperator.isEmptyString(greatRobotInfo)) {
			greatRobotInfoMap = SerializeHelper.stringToMap(greatRobotInfo);
		}
		if (!HawkOSOperator.isEmptyString(stealRobotInfo)) {
			stealRobotInfoMap = SerializeHelper.stringToMap(stealRobotInfo);
		}

	}
	
	public ResourceDefenseEntity() {
		
	}
	
	public ResourceDefenseEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.buyExpRefreshTime = HawkTime.getMillisecond();
		this.freeRefreshTimes = 0;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}
	
	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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

	public int getExp() {
		return exp;
	}

	public void addExp(int addExp) {
		this.exp += addExp; 
		notifyUpdate();
	}
	
	public String getStationInfo() {
		return stationInfo;
	}

	public void setStationInfo(String stationInfo) {
		this.stationInfo = stationInfo;
	}

	public boolean unlcokSuper() {
		return unlcokSuper == 1;
	}

	public void setUnlcokSuper(int unlcokSuper) {
		this.unlcokSuper = unlcokSuper;
	}

	public String getReceivedRewardId() {
		return receivedRewardId;
	}

	public void setReceivedRewardId(String receivedRewardId) {
		this.receivedRewardId = receivedRewardId;
	}

	public int getStealTimes() {
		return stealTimes;
	}

	public void setStealTimes(int stealTimes) {
		this.stealTimes = stealTimes;
	}

	public int getBeStealTimes() {
		return beStealTimes;
	}

	public void setBeStealTimes(int beStealTimes) {
		this.beStealTimes = beStealTimes;
	}

	public String getBuyExpInfo() {
		return buyExpInfo;
	}

	public void setBuyExpInfo(String buyExpInfo) {
		this.buyExpInfo = buyExpInfo;
	}

	public long getBuyExpRefreshTime() {
		return buyExpRefreshTime;
	}

	public void setBuyExpRefreshTime(long buyExpRefreshTime) {
		this.buyExpRefreshTime = buyExpRefreshTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	
	public List<ResourceDefenseTemplate> getStationInfoList() {
		return stationInfoList;
	}

	public void addStation(ResourceDefenseTemplate station) {
		stationInfoList.add(station);
		notifyUpdate();
	}

	public Set<Integer> getReceivedRewardIdSet() {
		return receivedRewardIdSet;
	}

	public void addReceivedRewardId(int rewardId) {
		receivedRewardIdSet.add(rewardId);
		notifyUpdate();
	}

	public Map<Integer, Integer> getBuyExpInfoMap() {
		return buyExpInfoMap;
	}

	public int getCanStealTimes() {
		return canStealTimes;
	}

	public void setCanStealTimes(int canStealTimes) {
		this.canStealTimes = Math.max(0, canStealTimes);
	}

	public long getStealTimesTick() {
		return stealTimesTick;
	}

	public void setStealTimesTick(long stealTimesTick) {
		this.stealTimesTick = stealTimesTick;
	}

	public long getStealTimesZeroTick() {
		return stealTimesZeroTick;
	}

	public void setStealTimesZeroTick(long stealTimesZeroTick) {
		this.stealTimesZeroTick = stealTimesZeroTick;
	}

	public Map<Integer, Integer> getAgentSkillInfoMap() {
		return agentSkillInfoMap;
	}

	public void setAgentSkillInfoMap(Map<Integer, Integer> agentSkillInfoMap) {
		this.agentSkillInfoMap = agentSkillInfoMap;
		this.notifyUpdate();
	}
	//增加技能使用次数一次
	public void addAgentSkillUseTimes(int skillId) {
		int beforeValue = this.agentSkillInfoMap.getOrDefault(skillId, 0);
		agentSkillInfoMap.put(skillId, beforeValue + 1);
		this.notifyUpdate();
	}

	public void clearAgentSkillUseTimes() {
		agentSkillInfoMap.clear();
		this.notifyUpdate();
	}

	public int getActiveSkill() {
		return activeSkill;
	}
	public boolean isActiveSkill() {
		return activeSkill == 1;
	}

	public void setActiveSkill(int activeSkill) {
		this.activeSkill = activeSkill;
	}

	public int getSkillRefreshTimes() {
		return skillRefreshTimes;
	}

	public void setSkillRefreshTimes(int skillRefreshTimes) {
		this.skillRefreshTimes = skillRefreshTimes;
	}

	public void addSkillRefreshTimes() {
		this.skillRefreshTimes = skillRefreshTimes + 1;
		notifyUpdate();
	}

	public List<Long> getAgentRecordList() {
		return agentRecordList;
	}

	public void setAgentRecordList(List<Long> agentRecordList) {
		this.agentRecordList = agentRecordList;
	}

	/**
	 * 初始化特工履历记录
	 */
	public void initAgentRecord() {
		this.agentRecordList.add(HawkTime.getMillisecond());
		this.agentRecordList.add(0L);
		this.agentRecordList.add(0L);
	}
	/**
	 * 添加特工履历记录
	 * index = 0 特工产生时间
	 * index = 1 期间共保护自身资源站{0}次(他人偷取失败次数)  被偷失败就算
	 * index = 2 期间共帮助玩家偷取他人晶石{0}次(自身能力触发且偷取成功次数) 技能触发算
	 */
	public void updateAgentRecord(int index, long value) {
		if (this.unlcokSuper()){
			long beforeValue = getAgentRecord(index);
			this.agentRecordList.add(index, beforeValue + value);
			this.notifyUpdate();
		}
	}

	public long getAgentRecord(int index) {
		if (agentRecordList.size() < index + 1){
			return 0;
		}
		return this.agentRecordList.get(index);
	}

	public String getGreatRobotInfo() {
		return greatRobotInfo;
	}

	public void setGreatRobotInfo(String greatRobotInfo) {
		this.greatRobotInfo = greatRobotInfo;
	}

	public Map<Integer, Integer> getGreatRobotInfoMap() {
		return greatRobotInfoMap;
	}


	public Map<Integer, Integer> getStealRobotInfoMap() {
		return stealRobotInfoMap;
	}

	public void setStealRobotInfoMap(Map<Integer, Integer> stealRobotInfoMap) {
		this.stealRobotInfoMap = stealRobotInfoMap;
	}

	/**
	 * 增加机器人偷取次数
	 * @param robotType
	 */
	public void addRobotStealTimes(int robotType) {
		int beforeTimes = stealRobotInfoMap.getOrDefault(robotType, 0);
		stealRobotInfoMap.put(robotType, beforeTimes + 1);
		notifyUpdate();
	}


	/**
	 * 增加高级机器人偷取次数, 高级机器人在偷取界面要返回真实的数据
	 * @param robotId
	 */
	public void addGreatRobotStealTimes(int robotId) {
		int beforeTimes = greatRobotInfoMap.getOrDefault(robotId, 0);
		greatRobotInfoMap.put(robotId, beforeTimes + 1);
		notifyUpdate();
	}
	public int getFreeRefreshTimes() {
		return freeRefreshTimes;
	}

	public void setFreeRefreshTimes(int freeRefreshTimes) {
		this.freeRefreshTimes = freeRefreshTimes;
	}

	public void setCdTime(long cdTime) {
		this.cdTime = cdTime;
	}

	public long getCdTime() {
		return cdTime;
	}
}
