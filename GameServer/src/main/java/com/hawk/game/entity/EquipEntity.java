package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.config.EquipmentCfg;

/**
 * 装备实体对象
 *
 * @author Jesse
 */
@Entity
@Table(name = "equip")
public class EquipEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId;

	@Column(name = "cfgId", nullable = false)
    @IndexProp(id = 3)
	private int cfgId;

	@Column(name = "state", nullable = false)
    @IndexProp(id = 4)
	private int state;

	@Column(name = "isNew", nullable = false)
    @IndexProp(id = 5)
	private boolean isNew = true;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 7)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 8)
	protected boolean invalid;

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
		this.isNew = true;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
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

	public Map<Integer, Integer> getEquipAttr() {
		Map<Integer, Integer> attrMap = new HashMap<>();
		EquipmentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, this.cfgId);
		if (cfg != null) {
			attrMap = cfg.getAttrMap();
		}
		return attrMap;
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
