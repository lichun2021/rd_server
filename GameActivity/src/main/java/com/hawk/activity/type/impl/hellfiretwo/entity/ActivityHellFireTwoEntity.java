package com.hawk.activity.type.impl.hellfiretwo.entity; 

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
import com.hawk.serialize.string.SerializeHelper;

/**
*	地狱火
*	auto generate do not modified
*/
@Entity
@Table(name="activity_hell_fire_two")
public class ActivityHellFireTwoEntity extends HawkDBEntity implements IActivityDataEntity{

	/**唯一ID*/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/**玩家ID*/
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false)
	private String playerId;

	/**活动的周期ID*/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false)
	private int termId;

	/**当前周期开始的时间*/
    @IndexProp(id = 4)
	@Column(name="cycleStartTime", nullable = false)
	private int cycleStartTime;

	/***/
    @IndexProp(id = 5)
	@Column(name="score", nullable = false)
	private int score;

	/***/
    @IndexProp(id = 6)
	@Column(name="initBuildingBattlePoint", nullable = false)
	private int initBuildingBattlePoint;

	/***/
    @IndexProp(id = 7)
	@Column(name="initTechBattlePoint", nullable = false)
	private int initTechBattlePoint;

	/***/
    @IndexProp(id = 8)
	@Column(name="otherSumScore", nullable = false)
	private int otherSumScore;

	/**targetId,isFinish*/
    @IndexProp(id = 9)
	@Column(name="targetIds", nullable = false)
	private String targetIds;

	/***/
    @IndexProp(id = 10)
	@Column(name="createTime", nullable = false)
	private long createTime;

	/***/
    @IndexProp(id = 11)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

	/***/
    @IndexProp(id = 12)
	@Column(name="invalid", nullable = false)
	private boolean invalid;

	/** complex type @targetIds*/
	@Transient
	private Map<Integer, Integer> targetIdsMap;

	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getCycleStartTime() {
		return this.cycleStartTime; 
	}

	public void setCycleStartTime(int cycleStartTime) {
		this.cycleStartTime = cycleStartTime;
	}

	public int getScore() {
		return this.score; 
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getInitBuildingBattlePoint() {
		return this.initBuildingBattlePoint; 
	}

	public void setInitBuildingBattlePoint(int initBuildingBattlePoint) {
		this.initBuildingBattlePoint = initBuildingBattlePoint;
	}

	public int getInitTechBattlePoint() {
		return this.initTechBattlePoint; 
	}

	public void setInitTechBattlePoint(int initTechBattlePoint) {
		this.initTechBattlePoint = initTechBattlePoint;
	}

	public int getOtherSumScore() {
		return this.otherSumScore; 
	}

	public void setOtherSumScore(int otherSumScore) {
		this.otherSumScore = otherSumScore;
	}

	public String getTargetIds() {
		return this.targetIds; 
	}

	public void setTargetIds(String targetIds) {
		this.targetIds = targetIds;
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

	public Map<Integer, Integer> getTargetIdsMap() {
		return this.targetIdsMap; 
	}

	public void setTargetIdsMap(Map<Integer, Integer> targetIdsMap) {
		this.targetIdsMap = targetIdsMap;
	}

	@Override
	public void afterRead() {		
		this.targetIdsMap = SerializeHelper.stringToMap(targetIds, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {		
		this.targetIds = SerializeHelper.mapToString(targetIdsMap);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
