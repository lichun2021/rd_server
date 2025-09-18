package com.hawk.activity.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityState;



/**
 * 活动实体对象
 * 
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity")
public class ActivityEntity extends HawkDBEntity implements IActivityEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	private String id = null;

	@Column(name = "activityId", nullable = false)
	private int activityId;

	@Column(name = "state", nullable = false)
	private int state;

	@Column(name = "termId", nullable = false)
	private int termId;
	
	@Column(name = "newlyTime", nullable = false)
	private long newlyTime;
	
	@Column(name = "createTime", nullable = false)
	protected long createTime = 0;

	@Column(name = "updateTime")
	protected long updateTime = 0;

	@Column(name = "invalid")
	protected boolean invalid;
	
	public ActivityEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public long getNewlyTime() {
		return newlyTime;
	}

	public void setNewlyTime(long newlyTime) {
		this.newlyTime = newlyTime;
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

	public ActivityState getActivityState() {
		return ActivityState.getState(state);
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
