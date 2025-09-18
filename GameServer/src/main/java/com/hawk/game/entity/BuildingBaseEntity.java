package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

/**
 * 建筑实体对象
 *
 * @author julia
 */
@Entity
@Table(name = "building")
public class BuildingBaseEntity extends HawkDBEntity {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
    private String id = null;

    @Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
    private String playerId = "";

    @Column(name = "buildingCfgId", nullable = false)
    @IndexProp(id = 3)
    private int buildingCfgId;

    @Column(name = "type", nullable = false)
    @IndexProp(id = 4)
    private int type;

    @Column(name = "buildIndex", nullable = false)
    @IndexProp(id = 5)
    private String buildIndex;

    @Column(name = "resUpdateTime")
    @IndexProp(id = 6)
    private long resUpdateTime;

    @Column(name = "status", nullable = false)
    @IndexProp(id = 7)
    private int status;

    @Column(name = "hp", nullable = false)
    @IndexProp(id = 8)
    private int hp;
    
    // 最后一次资源收取时间(初始时间为建筑新健时间) 已扩展至城内所有资源建筑
    @Column(name = "lastResCollectTime")
    @IndexProp(id = 9)
    private long lastResCollectTime = 0;
    
    /**
     * 最后一次升级的时间
     */
    @Column(name = "lastUpgradeTime")
    @IndexProp(id = 10)
    private long lastUpgradeTime = 0;
    
    /**
     * 超时空急救站建筑冷却恢复倒计时信息: 高16位存变更的时间日期，低16位存策划配置冷却时间序列的下标
     */
    @Column(name = "rescueCd")
    @IndexProp(id = 11)
    private int rescueCd;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 12)
    protected long createTime = 0;

    @Column(name = "updateTime")
    @IndexProp(id = 13)
    protected long updateTime = 0;

    @Column(name = "invalid")
    @IndexProp(id = 14)
    protected boolean invalid;

    public BuildingBaseEntity() {
    	// asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
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

    public int getBuildingCfgId() {
        return buildingCfgId;
    }

    public void setBuildingCfgId(int buildingCfgId) {
        this.buildingCfgId = buildingCfgId;
    }

    public String getBuildIndex() {
        return buildIndex;
    }

    public void setBuildIndex(String buildIndex) {
        this.buildIndex = buildIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getResUpdateTime() {
        return resUpdateTime;
    }

    public void setResUpdateTime(long resUpdateTime) {
        this.resUpdateTime = resUpdateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 百分比hp . 100 为满血
     * @return
     */
    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public long getLastResCollectTime() {
        return lastResCollectTime;
    }

    public void setLastResCollectTime(long lastResCollectTime) {
        this.lastResCollectTime = lastResCollectTime;
    }
    
    public long getLastUpgradeTime() {
		return lastUpgradeTime;
	}

	public void setLastUpgradeTime(long lastUpgradeTime) {
		this.lastUpgradeTime = lastUpgradeTime;
	}
	
	public int getRescueCd() {
		return rescueCd;
	}

	public void setRescueCd(int rescueCd) {
		this.rescueCd = rescueCd;
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
	
	public String getOwnerKey() {
		return playerId;
	}
}
