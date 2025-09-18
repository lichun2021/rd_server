package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.player.laboratory.Laboratory;

@Entity
@Table(name = "laboratory")
public class LaboratoryEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String id;

	@Column(name = "pageIndex")
	@IndexProp(id = 2)
	private int pageIndex;

	@Column(name = "powerCoreStr", nullable = false)
	@IndexProp(id = 3)
	private String powerCoreStr;

	@Column(name = "powerBlockStr", nullable = false)
	@IndexProp(id = 4)
	private String powerBlockStr;

	@Column(name = "pageUnlock", nullable = false)
	@IndexProp(id = 5)
	private int pageUnlock;

	@Column(name = "playerId", nullable = false)
	@IndexProp(id = 101)
	private String playerId = "";

	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 102)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 103)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 104)
	protected boolean invalid;

	@Transient
	private Laboratory labObj;

	@Override
	public void beforeWrite() {
		if (null != labObj) {
			this.powerCoreStr = labObj.serializPowerCore();
			this.powerBlockStr = labObj.serializPowerBlock();
		}
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		Laboratory.create(this);
		super.afterRead();
	}

	public Laboratory getLabObj() {
		if (!labObj.isEfvalLoad()) {
			labObj.loadEffVal();
		}
		return labObj;
	}

	public void recordLabObj(Laboratory labObj) {
		this.labObj = labObj;
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

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public String getPowerCoreStr() {
		return powerCoreStr;
	}

	public void setPowerCoreStr(String powerCoreStr) {
		this.powerCoreStr = powerCoreStr;
	}

	public String getPowerBlockStr() {
		return powerBlockStr;
	}

	public void setPowerBlockStr(String powerBlockStr) {
		this.powerBlockStr = powerBlockStr;
	}

	public int getPageUnlock() {
		return pageUnlock;
	}

	public void setPageUnlock(int pageUnlock) {
		this.pageUnlock = pageUnlock;
	}

}
