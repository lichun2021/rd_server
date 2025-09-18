package com.hawk.game.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;

import com.hawk.game.GsConfig;
import com.hawk.game.config.NationShipFactoryCfg;
import com.hawk.game.protocol.National.ShipComponents;

/**
 * 飞船制造厂
 * @author zhenyu.shang
 * @since 2022年4月21日
 */
@Entity
@Table(name = "nation_ship_component")
public class NationShipComponentEntity extends HawkDBEntity {
	
	@Id
	@Column(name = "componentId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private int componentId;
	
	@Column(name = "level")
    @IndexProp(id = 2)
	private int level;
	
	@Column(name = "upEndTime")
    @IndexProp(id = 3)
	private long upEndTime;
	
	@Column(name = "createTime")
    @IndexProp(id = 4)
	private long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 5)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 6)
	private boolean invalid;
	
	
	public NationShipComponentEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public int getComponentId() {
		return componentId;
	}
	
	public ShipComponents getComponent(){
		return ShipComponents.valueOf(componentId);
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getUpEndTime() {
		return upEndTime;
	}

	public void setUpEndTime(long upEndTime) {
		this.upEndTime = upEndTime;
	}

	@Override
	public String getPrimaryKey() {
		return String.valueOf(this.componentId);
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
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

	public NationShipFactoryCfg getCfg(){
		int baseId = this.componentId * 100 + level;
		NationShipFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationShipFactoryCfg.class, baseId);
		return cfg;
	}
	
	public NationShipFactoryCfg getNextCfg(){
		int baseId = this.componentId * 100 + level + 1;
		NationShipFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationShipFactoryCfg.class, baseId);
		return cfg;
	}
}
