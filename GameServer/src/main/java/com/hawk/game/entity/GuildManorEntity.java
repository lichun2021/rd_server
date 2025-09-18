package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.GsConfig;
import com.hawk.game.protocol.GuildManor.GuildManorStat;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月6日
 */
@Entity
@Table(name = "guild_manor")
public class GuildManorEntity extends HawkDBEntity {
	
	@Id
	@Column(name = "manorId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String manorId;
	
	@Column(name = "guildId")
    @IndexProp(id = 2)
	private String guildId;
	
	@Column(name = "manorIndex")
    @IndexProp(id = 3)
	private int manorIndex;
	
	@Column(name = "manorName")
    @IndexProp(id = 4)
	private String manorName;
	
	@Column(name = "manorState")
    @IndexProp(id = 5)
	private int manorState;
	
	@Column(name = "pos")
    @IndexProp(id = 6)
	private String pos;
	
	@Column(name = "level")
    @IndexProp(id = 7)
	private int level;
	
	@Column(name = "buildingLife")
    @IndexProp(id = 8)
	private double buildingLife;
	
	@Column(name = "completeTime")
    @IndexProp(id = 9)
	private long completeTime;
	
	@Column(name = "lastTakeBackTime")
    @IndexProp(id = 10)
	private long lastTakeBackTime;
	
	@Column(name = "placeTime")
    @IndexProp(id = 11)
	private long placeTime;
	
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

	public GuildManorEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}
	
	public String getManorId() {
		return manorId;
	}

	public void setManorId(String manorId) {
		this.manorId = manorId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getManorIndex() {
		return manorIndex;
	}

	public void setManorIndex(int manorIndex) {
		this.manorIndex = manorIndex;
	}

	public String getManorName() {
		return manorName;
	}

	public void setManorName(String manorName) {
		this.manorName = manorName;
	}

	public int getManorState() {
		return manorState;
	}

	public void setManorState(int manorState) {
		this.manorState = manorState;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}
	
	public double getBuildingLife() {
		return buildingLife;
	}

	public void setBuildingLife(double buildingLife) {
		this.buildingLife = buildingLife;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(long completeTime) {
		this.completeTime = completeTime;
	}

	public long getLastTakeBackTime() {
		return lastTakeBackTime;
	}

	public void setLastTakeBackTime(long lastTakeBackTime) {
		this.lastTakeBackTime = lastTakeBackTime;
	}

	public long getPlaceTime() {
		return placeTime;
	}

	public void setPlaceTime(long placeTime) {
		this.placeTime = placeTime;
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
	
	public GuildManorStat getManorStatEnum(){
		return GuildManorStat.valueOf(manorState);
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
		return this.manorId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException();
	}
}
