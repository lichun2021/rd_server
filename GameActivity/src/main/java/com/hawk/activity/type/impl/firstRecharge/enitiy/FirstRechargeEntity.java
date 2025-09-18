package com.hawk.activity.type.impl.firstRecharge.enitiy;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;

@Entity
@Table(name = "activity_first_recharge")
public class FirstRechargeEntity extends HawkDBEntity implements IActivityDataEntity{
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
	
    @IndexProp(id = 4)
	@Column(name = "hasReceiveReward", nullable = false)
	private int hasReceiveReward;
	
    @IndexProp(id = 5)
	@Column(name = "hasExtrAward", nullable = false)
	private int hasExtrAward;
	
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	public FirstRechargeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.hasReceiveReward = 0;
	}

	public FirstRechargeEntity() {
		
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

	public boolean hasReceiveReward() {
		return hasReceiveReward == 1;
	}

	public void setReceiveReward() {
		this.hasReceiveReward = 1;
	}

	public int getHasExtrAward() {
		return hasExtrAward;
	}

	public void setHasExtrAward(int hasExtrAward) {
		this.hasExtrAward = hasExtrAward;
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
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
