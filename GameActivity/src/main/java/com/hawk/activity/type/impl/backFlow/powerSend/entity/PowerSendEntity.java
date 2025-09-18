package com.hawk.activity.type.impl.backFlow.powerSend.entity;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;

@Entity
@Table(name = "activity_power_send")
public class PowerSendEntity  extends AchieveActivityEntity implements IActivityDataEntity {

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
	 * 回流次数
	 */
    @IndexProp(id = 4)
	@Column(name = "backCount", nullable = false)
	private int backCount;
	
	/**
	 * 发送次数
	 */
    @IndexProp(id = 5)
	@Column(name = "sendCount", nullable = false)
	private int sendCount;
	
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 6)
	@Column(name = "backType", nullable = false)
	private int backType;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 7)
	@Column(name = "overTime", nullable = false)
	private long overTime;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 8)
	@Column(name = "startTime", nullable = false)
	private long startTime;
	
    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	
	public PowerSendEntity() {
		
	}

	public PowerSendEntity(String playerId) {
		this.playerId = playerId;
		
	}
	
	public PowerSendEntity(String playerId, int termId) {
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
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
		
	}
	
	@Override
	public void afterRead() {
		
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	
	



	
	public int getBackType() {
		return backType;
	}

	public void setBackType(int backType) {
		this.backType = backType;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void addSendCount(int count){
		if(count <= 0){
			return;
		}
		this.sendCount += count;
		this.notifyUpdate();
	}
	
	
	
}
