package com.hawk.activity.type.impl.healexchange.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_heal_exchange")
public class HealExchangeEntity extends ActivityDataEntity {
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
	@Column(name = "loginDays", nullable = false)
	private String loginDays = "";

	/** 是否激活   */
    @IndexProp(id = 5)
	@Column(name = "active", nullable = false)
	private long active;

    @IndexProp(id = 6)
	@Column(name = "exchangeMsg", nullable = false)
	private String exchangeMsg = "";

    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();

	public HealExchangeEntity() {
	}

	public HealExchangeEntity(String playerId) {
		this.playerId = playerId;
		this.loginDays = "";
	}

	public HealExchangeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.loginDays = "";
	}

	public int getExchangeCount(int exchangeId) {
		return this.exchangeNumMap.getOrDefault(exchangeId, 0);
	}

	public void addExchangeCount(int eid, int count) {
		if (count <= 0) {
			return;
		}
		count += this.getExchangeCount(eid);
		this.exchangeNumMap.put(eid, count);
		this.notifyUpdate();
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

	@Override
	public void afterRead() {
		SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class, this.exchangeNumMap);
	}

	@Override
	public void beforeWrite() {
		this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;

	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public void setLoginDaysStr(String loginDays) {
		this.loginDays = loginDays;
	}

	@Override
	public String getLoginDaysStr() {
		return loginDays;
	}

	@Override
	public void recordLoginDay() {
		super.recordLoginDay();
	}

	public String getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(String loginDays) {
		this.loginDays = loginDays;
	}

	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}

	public long getActive() {
		return active;
	}

	public void setActive(long active) {
		this.active = active;
	}

}
