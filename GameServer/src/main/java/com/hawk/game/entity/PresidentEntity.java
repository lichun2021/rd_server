package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.GsConfig;

/**
 * 国王战实体对象
 *
 * @author hawk
 */
@Entity
@Table(name = "president")
public class PresidentEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "turnCount")
    @IndexProp(id = 2)
	private int turnCount;

	@Column(name = "presidentId")
    @IndexProp(id = 3)
	private String presidentId;

	@Column(name = "presidentGuildId")
    @IndexProp(id = 4)
	private String presidentGuildId;

	@Column(name = "countryModify")
    @IndexProp(id = 5)
	private int countryModify;

	@Column(name = "countryName")
    @IndexProp(id = 6)
	private String countryName;

	@Column(name = "countryIcon")
    @IndexProp(id = 7)
	private int countryIcon;

	@Column(name = "attackerId")
    @IndexProp(id = 8)
	private String attackerId;

	@Column(name = "attackerGuildId")
    @IndexProp(id = 9)
	private String attackerGuildId;

	@Column(name = "periodType")
    @IndexProp(id = 10)
	private int periodType;

	@Column(name = "startTime")
    @IndexProp(id = 11)
	private long startTime;

	@Column(name = "attackTime")
    @IndexProp(id = 12)
	private long attackTime;

	@Column(name = "peaceTime")
    @IndexProp(id = 13)
	private long peaceTime;

	@Column(name = "tenureTime")
    @IndexProp(id = 14)
	private long tenureTime;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 15)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 16)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 17)
	protected boolean invalid;

	public PresidentEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTurnCount() {
		return turnCount;
	}

	public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
	}

	public String getPresidentId() {
		return presidentId;
	}

	public void setPresidentId(String presidentId) {
		this.presidentId = presidentId;
	}

	public String getPresidentGuildId() {
		return presidentGuildId;
	}

	public int getCountryModify() {
		return countryModify;
	}

	public void setCountryModify(int countryModify) {
		this.countryModify = countryModify;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int getCountryIcon() {
		return countryIcon;
	}

	public void setCountryIcon(int countryIcon) {
		this.countryIcon = countryIcon;
	}

	public void setPresidentGuildId(String presidentGuildId) {
		this.presidentGuildId = presidentGuildId;
	}

	public String getAttackerId() {
		return attackerId;
	}

	public void setAttackerId(String attackerId) {
		this.attackerId = attackerId;
	}

	public String getAttackerGuildId() {
		return attackerGuildId;
	}

	public void setAttackerGuildId(String attackerGuildId) {
		this.attackerGuildId = attackerGuildId;
	}

	public int getPeriodType() {
		return periodType;
	}

	public void setPeriodType(int periodType) {
		this.periodType = periodType;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getAttackTime() {
		return attackTime;
	}

	public void setAttackTime(long attackTime) {
		this.attackTime = attackTime;
	}

	public long getPeaceTime() {
		return peaceTime;
	}

	public void setPeaceTime(long peaceTime) {
		this.peaceTime = peaceTime;
	}

	public long getTenureTime() {
		return tenureTime;
	}

	public void setTenureTime(long tenureTime) {
		this.tenureTime = tenureTime;
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
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
}
