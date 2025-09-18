package com.hawk.game.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hawk.game.protocol.WarCollege.WarCollegeInfo;
import com.hawk.game.protocol.WarCollege.WarCollegeInstance;
import com.hawk.serialize.string.SerializeHelper;

/** auto generate do not modified */
@Entity
@Table(name = "player_war_college")
public class PlayerWarCollegeEntity extends HawkDBEntity {

	/***/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String playerId;

	/** null */
	@Column(name = "instanceInfo", nullable = false, length = 1024)
	@IndexProp(id = 2)
	private String instanceInfo;

	@Column(name = "maxInstanceId", nullable = false)
	@IndexProp(id = 3)
	private int maxInstanceId;

	/***/
	@Column(name = "createTime", nullable = false, length = 19)
	@IndexProp(id = 7)
	private long createTime;

	/***/
	@Column(name = "updateTime", nullable = false, length = 19)
	@IndexProp(id = 8)
	private long updateTime;

	/***/
	@Column(name = "invalid", nullable = false, length = 0)
	@IndexProp(id = 9)
	private boolean invalid;
	
	/***/
	@Column(name = "firstReward", nullable = false, length = 0)
	@IndexProp(id = 10)
	private String firstReward;
	
	@Column(name = "helpRwardCount", nullable = false, length = 0)
	@IndexProp(id = 11)
	private int helpRwardCount; 
	
	@Column(name = "helpRwardDay", nullable = false, length = 0)
	@IndexProp(id = 12)
	private int helpRwardDay;
	
	@Transient
	private Map<Integer,Long> firstRewardMap;

	// {副本id：[攻打（赢）次数，最后攻打时间(Date)，领奖次数]}
	@Transient
	private Map<Integer, JSONObject> instanceInfoMap;

	@Transient
	private static final String hitCount = "hitCount"; // 打副本胜利次数
	@Transient
	private static final String lastTime = "lastTime"; // 最后一次打副本时间
	@Transient
	private static final String rewardCount = "rewardCount"; // 领奖次数
	@Transient
	private static final String exterminateCount = "exterminateCount"; // 剿灭
	

	public boolean hitInstance(int instanceId) {
		return maxInstanceId >= instanceId;
	}

	public int getHitCount(int instanceId) {
		try {
			return instanceInfoMap.get(instanceId).getIntValue(hitCount);
		} catch (Exception e) {
			return 0;
		}
	}

	public long getLastTime(int instanceId) {
		try {
			return instanceInfoMap.get(instanceId).getLongValue(lastTime);
		} catch (Exception e) {
			return 0;
		}
	}

	public int getRewardCount(int instanceId) {
		try {
			return instanceInfoMap.get(instanceId).getIntValue(rewardCount);
		} catch (Exception e) {
			return 0;
		}
	}

	public String getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getInstanceInfo() {
		return this.instanceInfo;
	}

	public void setInstanceInfo(String instanceInfo) {
		this.instanceInfo = instanceInfo;
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

	@Override
	public void afterRead() {
		instanceInfoMap = new ConcurrentHashMap<>();
		if (!HawkOSOperator.isEmptyString(instanceInfo)) {
			try {				
				@SuppressWarnings("unchecked")
				Map<Integer, JSONObject> jsonMap = JSONObject.parseObject(instanceInfo, instanceInfoMap.getClass());
				instanceInfoMap.putAll(jsonMap);				
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
			
		}
		
		this.firstRewardMap = new ConcurrentHashMap<>(); 
		if (!HawkOSOperator.isEmptyString(this.firstReward)) {
			SerializeHelper.stringToMap(this.firstReward, Integer.class, Long.class, this.firstRewardMap);
		}
	}

	@Override
	public void beforeWrite() {
		this.instanceInfo = JSON.toJSONString(instanceInfoMap);
		this.firstReward = SerializeHelper.mapToString(this.firstRewardMap);
	}

	/** 增加打副本次数
	 * 
	 * @param instanceId
	 * @param count */
	public void addInstanceHitCount(Integer instanceId) {
		maxInstanceId = Math.max(maxInstanceId, instanceId);
		if (!this.instanceInfoMap.containsKey(instanceId)) {
			JSONObject json = new JSONObject();
			json.put(hitCount, 1);
			json.put(lastTime, HawkTime.getMillisecond());
			json.put(rewardCount, 0);
			this.instanceInfoMap.put(instanceId, json);
		} else {
			JSONObject json = this.instanceInfoMap.get(instanceId);
			json.put(hitCount, json.getIntValue(hitCount) + 1);
		}
		this.notifyUpdate();
	}

	/** 增加剿灭副本次数
	 * 
	 * @param instanceId
	 * @param count */
	public void addInstanceExterminateCount(Integer instanceId) {
		maxInstanceId = Math.max(maxInstanceId, instanceId);
		if (!this.instanceInfoMap.containsKey(instanceId)) {
			JSONObject json = new JSONObject();
			json.put(hitCount, 0);
			json.put(lastTime, HawkTime.getMillisecond());
			json.put(rewardCount,0);
			json.put(exterminateCount,1);
			this.instanceInfoMap.put(instanceId, json);
		} else {
			JSONObject json = this.instanceInfoMap.get(instanceId);
			json.put(exterminateCount, json.getIntValue(exterminateCount) + 1);
		}
		this.notifyUpdate();
	}

	
	
	
	public void checkOrReset() {
		List<Boolean> list = Lists.newArrayList();
		for (int instanceId : instanceInfoMap.keySet()) {
			if (checkOrReset(instanceId)) {
				list.add(true);
			}
		}
		if (!list.isEmpty()){
			this.notifyUpdate();
		}
	}

	public boolean checkOrReset(int instanceId) {
		if (!isSameDay(instanceId)) {
			removeInstanceInfo(instanceId);
			return true;
		}
		return false;
	}

	private void removeInstanceInfo(Integer instanceId) {
		this.instanceInfoMap.remove(instanceId);
		this.notifyUpdate();
	}

	/** 副本最後打過的時間是否已經過去一天
	 * 
	 * @param instanceId
	 *            副本ID
	 * @return */
	private boolean isSameDay(int instanceId) {
		return !instanceInfoMap.containsKey(instanceId) ? false : HawkTime.isSameDay(getLastTime(instanceId), HawkTime.getMillisecond());
	}

	/** 记录领奖次数
	 * 
	 * @param instanceId */
	public void addRewardCount(int instanceId) {
		JSONObject json = instanceInfoMap.get(instanceId);
		if (json == null) {
			return;
		}
		json.put(rewardCount, json.getIntValue(rewardCount) + 1);
		this.notifyUpdate();
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("PlayerWarCollegeEntity can not set primaryKey");

	}

	public Map<Integer, JSONObject> getInstanceInfoMap() {
		return instanceInfoMap;
	}

	public void setInstanceInfoMap(Map<Integer, JSONObject> instanceInfoMap) {
		this.instanceInfoMap = instanceInfoMap;
	}

	public String getOwnerKey() {
		return playerId;
	}

	public int getMaxInstanceId() {
		return maxInstanceId;
	}

	public void setMaxInstanceId(int maxInstanceId) {
		this.maxInstanceId = maxInstanceId;
	}

	
	public Map<Integer, Long> getFirstRewardMap() {
		return firstRewardMap;
	}
	
	
	public boolean instanceHasFirstReward(int instanceId){
		if(this.firstRewardMap.containsKey(instanceId)){
			return true;
		}
		return false;
	}
	
	
	public boolean addFirstRewardRecord(int instanceId,long time){
		if(this.firstRewardMap.containsKey(instanceId) ){
			return false;
		}
		this.firstRewardMap.put(instanceId, time);
		this.notifyUpdate();
		return true;
	}
	
	
	public int getHelpRwardCount() {
		int today = HawkTime.getYearDay();
		if(today != this.helpRwardDay){
			return 0;
		}
		return helpRwardCount;
	}
	
	public void addHelpRwardCount(int count){
		int today = HawkTime.getYearDay();
		if(today != this.helpRwardDay){
			this.helpRwardDay = today;
			this.helpRwardCount = 0;
		}
		this.helpRwardCount += count;
		this.notifyUpdate();
	}
	
	public long getFirstRewardTime(int instanceId) {
		return this.firstRewardMap.getOrDefault(instanceId, 0l);
	}
	
	
	public WarCollegeInfo.Builder genWarCollegeInfoBuilder(){
		WarCollegeInfo.Builder builder = WarCollegeInfo.newBuilder();
		builder.setMaxInstanceId(this.getMaxInstanceId());
		for(Map.Entry<Integer, JSONObject> entry : this.instanceInfoMap.entrySet()){
			WarCollegeInstance.Builder inBuiler = WarCollegeInstance.newBuilder();
			int instanceId = entry.getKey();
			JSONObject obj = entry.getValue();
			inBuiler.setInstanceId(instanceId);
			inBuiler.setExterminateCount(obj.getIntValue(exterminateCount));
			inBuiler.setHitCount(obj.getIntValue(hitCount));
			builder.addWars(inBuiler);
		}
		return builder;
	}
}