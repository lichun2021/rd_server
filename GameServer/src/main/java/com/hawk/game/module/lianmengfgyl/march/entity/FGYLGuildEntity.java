package com.hawk.game.module.lianmengfgyl.march.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;


@Entity
@Table(name = "guild_fgyl")
public class FGYLGuildEntity extends HawkDBEntity {
	
	
	@Id
	@Column(name = "guildId", nullable = false)
	@IndexProp(id = 1)
	private String guildId = "";
	
	@Column(name = "passLevel", nullable = false)
	@IndexProp(id = 2)
	private int passLevel;
	
	@Column(name = "useTime", nullable = false)
	@IndexProp(id = 3)
	private int useTime;
	
	@Column(name = "passTime", nullable = false)
	@IndexProp(id = 4)
	private long passTime;
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 5)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 6)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 7)
	protected boolean invalid;
	
	
	
	
	public boolean addRecordMax(int level,int useTime,long passTime){
		if(this.passLevel < level || 
				(this.passLevel == level && useTime < this.useTime)){
			this.passLevel = level;
			this.useTime = useTime;
			this.passTime = passTime;
			this.notifyUpdate();
			return true;
		}
		return false;
	}
	


	public FGYLGuildEntity() {
		
	}

	@Override
	public void beforeWrite() {
		super.beforeWrite();
	}

	@Override
	public void afterRead() {
		super.afterRead();
	}
	
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}


	public String getGuildId() {
		return guildId;
	}
	
	
	public int getPassLevel() {
		return passLevel;
	}
	
	public void setPassLevel(int passLevel) {
		this.passLevel = passLevel;
	}
	
	public int getUseTime() {
		return useTime;
	}
	
	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}
	
	public long getPassTime() {
		return passTime;
	}
	
	public void setPassTime(long passTime) {
		this.passTime = passTime;
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
		return this.guildId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.guildId = primaryKey;

	}
	
	
	
	
	

	

}
