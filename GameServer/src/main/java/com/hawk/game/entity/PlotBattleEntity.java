package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
*	plot_battle
*	auto generate do not modified
*/
@Entity
@Table(name="plot_battle")
public class PlotBattleEntity extends HawkDBEntity{

	/**玩家ID*/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	/**关卡ID*/
	@Column(name="levelId", nullable = false)
    @IndexProp(id = 2)
	private int levelId;

	/**关卡的状态*/
	@Column(name="status", nullable = false)
    @IndexProp(id = 3)
	private int status;

	/**记录的创建时间*/
	@Column(name="createTime", nullable = false)
    @IndexProp(id = 4)
	private long createTime;

	/**更新时间*/
	@Column(name="updateTime", nullable = false)
    @IndexProp(id = 5)
	private long updateTime;

	/***/
	@Column(name="invalid", nullable = false)
    @IndexProp(id = 6)
	private boolean invalid;

	public String getPlayerId(){
		return this.playerId; 
	}

	public void setPlayerId(String playerId){
		this.playerId = playerId;
	}

	public int getLevelId(){
		return this.levelId; 
	}

	public void setLevelId(int levelId){
		this.levelId = levelId;
	}

	public int getStatus(){
		return this.status; 
	}

	public void setStatus(int status){
		this.status = status;
	}

	public long getCreateTime(){
		return this.createTime; 
	}

	public void setCreateTime(long createTime){
		this.createTime = createTime;
	}

	public long getUpdateTime(){
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime){
		this.updateTime = updateTime;
	}

	public boolean isInvalid(){
		return this.invalid; 
	}

	public void setInvalid(boolean invalid){
		this.invalid = invalid;
	}

	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("plot battle entity primaryKey is playerId");		
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}