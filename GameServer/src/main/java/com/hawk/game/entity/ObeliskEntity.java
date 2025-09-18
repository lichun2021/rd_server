package com.hawk.game.entity;


import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.protocol.Obelisk.PBObeliskPlayerState;

import javax.persistence.*;

/**
 * 方尖碑单条子对象entity
 * @author hf
 */
@Entity
@Table(name = "obelisk")
public class ObeliskEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 2)
	private String playerId = "";
	/** 任务id 和表一一对应 */
	@Column(name = "cfgId", nullable = false)
	@IndexProp(id = 3)
	private int cfgId;
	/** 状态 */
	@Column(name = "state", nullable = false)
	@IndexProp(id = 4)
	private int state;

	/** 贡献度   任务结束时与state一同被修改*/
	@Column(name = "contribution", nullable = false)
	@IndexProp(id = 5)
	protected int contribution = 0;

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 8)
	protected boolean invalid;

	public ObeliskEntity() {
		super();
	}
	public ObeliskEntity(String playerId, int cfgId, PBObeliskPlayerState state) {
		this.playerId = playerId;
		this.cfgId = cfgId;
		this.state = state.getNumber();
		this.contribution = -1;
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

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public PBObeliskPlayerState getState() {
		return PBObeliskPlayerState.valueOf(state);
	}

	public void setState(PBObeliskPlayerState state) {
		this.state = state.getNumber();
	}

	public int getContribution() {
		return contribution;
	}

	public void setContribution(int contribution) {
		this.contribution = contribution;
	}

	public void addContribution(int value){
		this.contribution = contribution+ value;
		notifyUpdate();
	}
	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public void setUpdateTime(long updateTime) {
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

	@Override
	public String getOwnerKey() {
		return playerId;
	}
}
