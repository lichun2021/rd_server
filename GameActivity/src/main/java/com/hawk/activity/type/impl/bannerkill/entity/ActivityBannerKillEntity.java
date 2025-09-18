package com.hawk.activity.type.impl.bannerkill.entity; 

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
 * 战神降临活动
 * 
 * @author lating
 *
 */
@Entity
@Table(name="activity_banner_kill")
public class ActivityBannerKillEntity extends HawkDBEntity implements IActivityDataEntity{

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

	/** 杀敌积分 */
    @IndexProp(id = 4)
	@Column(name="killEnemyScore", nullable = false)
	private long killEnemyScore;

	/**
	 * 杀敌目标积分信息：targetId,isFinish
	 * */
    @IndexProp(id = 5)
	@Column(name="killTargetInfo", nullable = false)
	private String killTargetInfo;

    @IndexProp(id = 6)
	@Column(name="createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name="updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name="invalid", nullable = false)
	private boolean invalid;

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

	public long getKillEnemyScore() {
		return killEnemyScore;
	}

	public void setKillEnemyScore(long killEnemyScore) {
		this.killEnemyScore = killEnemyScore;
	}

	public String getKillTargetInfo() {
		return killTargetInfo;
	}

	public void setKillTargetInfo(String killTargetInfo) {
		this.killTargetInfo = killTargetInfo;
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
		this.targetIdsMap = SerializeHelper.stringToMap(killTargetInfo, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {		
		this.killTargetInfo = SerializeHelper.mapToString(targetIdsMap);
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
