package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.guild.Counterattack;

/**
 * 联盟反击
 */
@Entity
@Table(name = "guild_counterattack")
public class GuildCounterattackEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "guildId", nullable = false)
    @IndexProp(id = 2)
	private String guildId = "";

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 3)
	private String playerId = ""; // 被打人id
	@Column(name = "lostPower")
    @IndexProp(id = 4)
	private int lostPower; // 损失战力

	@Column(name = "atkerId", nullable = false)
    @IndexProp(id = 5)
	private String atkerId = ""; // 攻击者id

	@Column(name = "counterPower")
    @IndexProp(id = 6)
	private int counterPower; // 达成反击需要消灭战力

	@Column(name = "attackerPointId")
    @IndexProp(id = 7)
	private int attackerPointId; // 战斗发生时, 攻击者坐标

	@Column(name = "playerBountySer", nullable = false)
    @IndexProp(id = 8)
	private String playerBountySer = ""; // 各人捐赠

	@Column(name = "wipeoutSer", nullable = false)
    @IndexProp(id = 9)
	private String wipeoutSer = ""; // 消灭记录

	@Column(name = "rewards", nullable = false)
    @IndexProp(id = 10)
	private String rewards;// 系统悬赏
	
	@Column(name = "bitBackRewards", nullable = false)
    @IndexProp(id = 11)
	private String bitBackRewards; // 反击达成返还

	@Column(name = "overTime", nullable = false)
    @IndexProp(id = 12)
	protected long overTime = 0;// 过期时间

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
	private boolean changed;
	@Transient
	private Counterattack counter;

	@Override
	public void beforeWrite() {
		if (Objects.nonNull(this.counter)) {
			this.playerBountySer = counter.playerBountySer();
			this.wipeoutSer = counter.wipeoutSer();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		Counterattack.create(this);
		super.afterRead();
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
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

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getAtkerId() {
		return atkerId;
	}

	public void setAtkerId(String atkerId) {
		this.atkerId = atkerId;
	}

	public String getPlayerBountySer() {
		return playerBountySer;
	}

	public void setPlayerBountySer(String playerBountySer) {
		this.playerBountySer = playerBountySer;
	}

	public String getWipeoutSer() {
		return wipeoutSer;
	}

	public void setWipeoutSer(String wipeoutSer) {
		this.wipeoutSer = wipeoutSer;
	}

	public int getLostPower() {
		return lostPower;
	}

	public void setLostPower(int lostPower) {
		this.lostPower = lostPower;
	}

	public int getCounterPower() {
		return counterPower;
	}

	public void setCounterPower(int counterPower) {
		this.counterPower = counterPower;
	}

	public Counterattack getCounter() {
		return counter;
	}

	public void setCounter(Counterattack counter) {
		this.counter = counter;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public int getAttackerPointId() {
		return attackerPointId;
	}

	public void setAttackerPointId(int attackerPointId) {
		this.attackerPointId = attackerPointId;
	}

	public void recordObj(Counterattack counter) {
		this.counter = counter;
	}

	public String getBitBackRewards() {
		return bitBackRewards;
	}

	public void setBitBackRewards(String bitBackRewards) {
		this.bitBackRewards = bitBackRewards;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
