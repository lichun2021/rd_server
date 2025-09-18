package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.guild.manor.building.IGuildBuilding;

/**
 * 联盟建筑实体
 * @author zhenyu.shang
 * @since 2017年7月7日
 */
@Entity
@Table(name = "guild_building")
public class GuildBuildingEntity extends HawkDBEntity{

	@Id
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id;
	
	@Column(name = "guildId")
    @IndexProp(id = 2)
	private String guildId;
	
	@Column(name = "buildType")
    @IndexProp(id = 3)
	private int buildType;
	
	@Column(name = "buildingId")
    @IndexProp(id = 4)
	private int buildingId;
	
	@Column(name = "buildingStat")
    @IndexProp(id = 5)
	private int buildingStat;
	
	@Column(name = "pos")
    @IndexProp(id = 6)
	private String pos;
	
	@Column(name = "buildTime")
    @IndexProp(id = 7)
	private long buildTime;
	
	@Column(name = "level")
    @IndexProp(id = 8)
	private int level;
	
	@Column(name = "buildLife")
    @IndexProp(id = 9)
	private double buildLife;
	
	@Column(name = "buildParam")
    @IndexProp(id = 10)
	private String buildParam;
	
	@Column(name = "lastTakeBackTime")
    @IndexProp(id = 11)
	private long lastTakeBackTime;
	
	@Column(name = "lastTickTime")
    @IndexProp(id = 12)
	private long lastTickTime;
	
	@Column(name = "createTime")
    @IndexProp(id = 13)
	private long createTime;

	@Column(name = "updateTime")
    @IndexProp(id = 14)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 15)
	private boolean invalid;
	
	@Transient
	private IGuildBuilding buildingObj;
	
	// 通知buildParam数据发生变化
	@Transient
	private boolean changed;
	
	public GuildBuildingEntity() {
		this.buildLife = 0;
		// asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
	public int getBuildType() {
		return buildType;
	}

	public void setBuildType(int buildType) {
		this.buildType = buildType;
	}

	public int getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}

	public int getBuildingStat() {
		return buildingStat;
	}

	public void setBuildingStat(int buildingStat) {
		this.buildingStat = buildingStat;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public long getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getBuildLife() {
		return buildLife;
	}

	public void setBuildLife(double buildLife) {
		this.buildLife = buildLife;
	}

	public String getBuildParam() {
		return buildParam;
	}

	public void setBuildParam(String buildParam) {
		this.buildParam = buildParam;
	}

	public long getLastTakeBackTime() {
		return lastTakeBackTime;
	}

	public void setLastTakeBackTime(long lastTakeBackTime) {
		this.lastTakeBackTime = lastTakeBackTime;
	}
	/**
	 * 注意:buildParam数据发生变化,需手动调用,确保数据正常落地
	 * @param changed
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public long getLastTickTime() {
		return lastTickTime;
	}

	public void setLastTickTime(long lastTickTime) {
		this.lastTickTime = lastTickTime;
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
	
	public void storeBuilding(IGuildBuilding building){
		this.buildingObj = building;
		this.notifyUpdate();
	}
	
	@Override
	public void beforeWrite() {
		if(buildingObj != null){
			this.buildParam = this.buildingObj.genBuildingParamStr();
			this.changed = false;
		}
	}
	
	public int getPosX(){
		if(pos == null){
			return 0;
		}
		return Integer.parseInt(pos.split(",")[0]);
	}
	
	public int getPosY(){
		if(pos == null){
			return 0;
		}
		return Integer.parseInt(pos.split(",")[1]);
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
