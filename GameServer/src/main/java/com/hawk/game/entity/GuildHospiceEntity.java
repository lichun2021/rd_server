package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.module.hospice.HospiceObj;

@Entity
@Table(name = "guild_hospice")
public class GuildHospiceEntity extends HawkDBEntity {

	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "attackerId", nullable = false)
    @IndexProp(id = 3)
	private String attackerId = "";

	@Column(name = "overwhelming", nullable = false)
    @IndexProp(id = 4)
	private int overwhelming;

	@Column(name = "maxPower", nullable = false)
    @IndexProp(id = 5)
	private long maxPower;// 历史最高战力
	@Column(name = "lostPower", nullable = false)
    @IndexProp(id = 6)
	private long lostPower; // 累计损失战力. 补尝发放清0

	@Column(name = "matchStartTime", nullable = false)
    @IndexProp(id = 7)
	protected long matchStartTime = 0;
	@Column(name = "matchEndTime", nullable = false)
    @IndexProp(id = 8)
	protected long matchEndTime = 0;

	@Column(name = "state", nullable = false)
    @IndexProp(id = 9)
	private String state = "";

	@Column(name = "awards", nullable = false)
    @IndexProp(id = 10)
	private String awards = "";

	@Column(name = "helpQueue", nullable = false)
    @IndexProp(id = 11)
	private String helpQueue = "";

	@Column(name = "helpers", nullable = false)
    @IndexProp(id = 12)
	private String helpers = "";

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 13)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 14)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 15)
	protected boolean invalid;

	@Transient
	private HospiceObj hospiceObj;

	@Override
	public void afterRead() {
		HospiceObj obj = new HospiceObj();
		obj.setDbEntity(this);
		super.afterRead();
	}

	public long getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(long maxPower) {
		this.maxPower = maxPower;
	}

	public long getLostPower() {
		return lostPower;
	}

	public void setLostPower(long lostPower) {
		this.lostPower = lostPower;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public HospiceObj getHospiceObj() {
		return hospiceObj;
	}

	public void setHospiceObj(HospiceObj hospiceObj) {
		this.hospiceObj = hospiceObj;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHelpers() {
		return helpers;
	}

	public void setHelpers(String helpers) {
		this.helpers = helpers;
	}

	public long getMatchStartTime() {
		return matchStartTime;
	}

	public void setMatchStartTime(long matchStartTime) {
		this.matchStartTime = matchStartTime;
	}

	public long getMatchEndTime() {
		return matchEndTime;
	}

	public void setMatchEndTime(long matchEndTime) {
		this.matchEndTime = matchEndTime;
	}

	public String getHelpQueue() {
		return helpQueue;
	}

	public void setHelpQueue(String helpQueue) {
		this.helpQueue = helpQueue;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public String getAttackerId() {
		return attackerId;
	}

	public void setAttackerId(String attackerId) {
		this.attackerId = attackerId;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

	public int getOverwhelming() {
		return overwhelming;
	}

	public void setOverwhelming(int overwhelming) {
		this.overwhelming = overwhelming;
	}

	
	public String getOwnerKey() {
		return playerId;
	}
}
