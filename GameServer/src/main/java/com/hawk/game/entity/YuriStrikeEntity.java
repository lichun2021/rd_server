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

import com.hawk.game.GsConfig;
import com.hawk.game.yuriStrikes.YuriStrike;

@Entity
@Table(name = "yuri_strike")
public class YuriStrikeEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	@Column(name = "cfgId", nullable = false)
    @IndexProp(id = 3)
	private int cfgId;

	@Column(name = "areaIdLock", nullable = false)
    @IndexProp(id = 4)
	private int areaIdLock;// 锁点地块

	@Column(name = "marchId", nullable = false)
    @IndexProp(id = 5)
	private String marchId = "";

	@Column(name = "cleanQueueId", nullable = false)
    @IndexProp(id = 6)
	private String cleanQueueId = "";// 净化中, 队列id

	@Column(name = "state", nullable = false)
    @IndexProp(id = 7)
	private String state = "";

	@Column(name = "hasReward", nullable = false)
    @IndexProp(id = 8)
	private int hasReward;// 是否有净化后的兵没有收取

	@Column(name = "matchTime", nullable = false)
    @IndexProp(id = 9)
	protected long matchTime = 0; // 行军开始时间

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 10)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 11)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 12)
	protected boolean invalid;

	@Transient
	private YuriStrike YuriStrikeObj;

	public YuriStrikeEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	@Override
	public void beforeWrite() {
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		YuriStrike bean = new YuriStrike();
		bean.setDbEntity(this);
		super.afterRead();
	}

	public YuriStrike getYuriStrikeObj() {
		return YuriStrikeObj;
	}

	public void setYuriStrikeObj(YuriStrike yuriStrikeObj) {
		YuriStrikeObj = yuriStrikeObj;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getMatchTime() {
		return matchTime;
	}

	public void setMatchTime(long matchTime) {
		this.matchTime = matchTime;
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

	public int getAreaIdLock() {
		return areaIdLock;
	}

	public void setAreaIdLock(int areaIdLock) {
		this.areaIdLock = areaIdLock;
	}

	public String getCleanQueueId() {
		return cleanQueueId;
	}

	public void setCleanQueueId(String cleanQueueId) {
		this.cleanQueueId = cleanQueueId;
	}

	public int getHasReward() {
		return hasReward;
	}

	public void setHasReward(int hasReward) {
		this.hasReward = hasReward;
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
