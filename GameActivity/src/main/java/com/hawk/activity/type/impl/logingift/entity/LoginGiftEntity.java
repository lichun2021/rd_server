package com.hawk.activity.type.impl.logingift.entity;

import java.util.Set;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 新版新手登录活动
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_login_gift")
public class LoginGiftEntity extends HawkDBEntity implements IActivityDataEntity {

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

	/** 登录时间  */
    @IndexProp(id = 4)
	@Column(name = "loginDays", nullable = false)
	private String loginDays;

	/** 购买进阶礼包的时间 */
    @IndexProp(id = 5)
	@Column(name = "buyAdvanceTime", nullable = false)
	private long buyAdvanceTime;
	
	/** 已领取普通奖励的天数   */
    @IndexProp(id = 6)
	@Column(name = "receivedCommDays", nullable = false)
	private String receivedCommDays;
	
	/** 已领取进阶奖励的天数  */
    @IndexProp(id = 7)
	@Column(name = "receivedAdvanceDays", nullable = false)
	private String receivedAdvanceDays;
	
	/** 进阶礼包购买倒计时结束时间点 */
    @IndexProp(id = 8)
	@Column(name = "advanceEndTime", nullable = false)
	private long advanceEndTime;
	
    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private Set<Integer> loginDaySet = new ConcurrentHashSet<Integer>();

	@Transient
	private Set<Integer> receivedCommDaySet = new ConcurrentHashSet<Integer>();
	
	@Transient
	private Set<Integer> receivedAdvanceDaySet = new ConcurrentHashSet<Integer>();
	
	
	public LoginGiftEntity() {
	}

	public LoginGiftEntity(String playerId) {
		this.playerId = playerId;
	}

	public LoginGiftEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.loginDays = SerializeHelper.collectionToString(this.loginDaySet, SerializeHelper.ATTRIBUTE_SPLIT);
		this.receivedCommDays = SerializeHelper.collectionToString(this.receivedCommDaySet, SerializeHelper.ATTRIBUTE_SPLIT);
		this.receivedAdvanceDays = SerializeHelper.collectionToString(this.receivedAdvanceDaySet, SerializeHelper.ATTRIBUTE_SPLIT);
	}


	@Override
	public void afterRead() {
		if (!HawkOSOperator.isEmptyString(loginDays)) {
			this.loginDaySet = SerializeHelper.stringToSet(Integer.class, loginDays, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		
		if (!HawkOSOperator.isEmptyString(receivedCommDays)) {
			this.receivedCommDaySet = SerializeHelper.stringToSet(Integer.class, receivedCommDays, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		
		if (!HawkOSOperator.isEmptyString(receivedAdvanceDays)) {
			this.receivedAdvanceDaySet = SerializeHelper.stringToSet(Integer.class, receivedAdvanceDays, SerializeHelper.ATTRIBUTE_SPLIT);
		}
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
	
	public long getBuyAdvanceTime() {
		return buyAdvanceTime;
	}

	public void setBuyAdvanceTime(long buyAdvanceTime) {
		this.buyAdvanceTime = buyAdvanceTime;
	}
	
	public void addLoginDay(int loginDay) {
		boolean constains = this.loginDaySet.contains(loginDay);
		this.loginDaySet.add(loginDay);
		if (!constains) {
			this.notifyUpdate();
		}
	}
	
	public Set<Integer> getLoginDaySet() {
		return this.loginDaySet;
	}
	
	public int getLoginDayTotal() {
		return this.loginDaySet.size();
	}
	
	public void addCommReceivedDay(int receivedDay) {
		this.receivedCommDaySet.add(receivedDay);
	}
	
	public Set<Integer> getReceivedCommDaySet() {
		return this.receivedCommDaySet;
	}
	
	public void addAdvanceReceivedDay(int receivedDay) {
		this.receivedAdvanceDaySet.add(receivedDay);
	}
	
	public Set<Integer> getReceivedAdvanceDaySet() {
		return this.receivedAdvanceDaySet;
	}
	
	public long getAdvanceEndTime() {
		return advanceEndTime;
	}

	public void setAdvanceEndTime(long advanceEndTime) {
		this.advanceEndTime = advanceEndTime;
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
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
