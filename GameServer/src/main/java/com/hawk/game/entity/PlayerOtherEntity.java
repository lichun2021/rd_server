package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Transient;

import com.hawk.game.module.autologic.data.PlayerAutoMarchParam;
import com.hawk.serialize.string.SerializeHelper;

/**
*	playerId
*	auto generate do not modified
*/
@Entity
@Table(name="player_other")
public class PlayerOtherEntity extends HawkDBEntity{

	/***/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId;

	/**null*/
	@Column(name="dressItemInfo", length=1024)
	@IndexProp(id = 2)
	private String dressItemInfo;

	/***/
	@Column(name="createTime", nullable = false, length=19)
	@IndexProp(id = 3)
	private long createTime;

	/***/
	@Column(name="updateTime", nullable = false, length=19)
	@IndexProp(id = 4)
	private long updateTime;

	/***/
	@Column(name="invalid", nullable = false, length=0)
	@IndexProp(id = 5)
	private boolean invalid;


	
	@Column(name = "autoGuildCityMoveCnt")
	@IndexProp(id = 6)
	private int autoGuildCityMoveCnt;
	

	@Column(name = "autoMarchParam")
	@IndexProp(id = 7)
	private String autoMarchParam= "";
	

	/** complex type @dressItemInfo*/
	@Transient
	private Map<Integer, Integer> dressItemInfoMap = new HashMap<>();
	
	@Transient
	PlayerAutoMarchParam playerAutoMarchParam = new PlayerAutoMarchParam();
	

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getDressItemInfo() {
		return this.dressItemInfo; 
	}

	public void setDressItemInfo(String dressItemInfo) {
		this.dressItemInfo = dressItemInfo;
	}

	public long getCreateTime() {
		return this.createTime; 
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return this.invalid; 
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public Map<Integer, Integer> getDressItemInfoMap() {
		return this.dressItemInfoMap; 
	}

	public void setDressItemInfoMap(Map<Integer, Integer> dressItemInfoMap) {
		this.dressItemInfoMap = dressItemInfoMap;
	}

	@Override
	public void afterRead() {		
		this.dressItemInfoMap = SerializeHelper.stringToMap(dressItemInfo, Integer.class, Integer.class);
		//玩家自动行军参数
		PlayerAutoMarchParam playerAutoMarchParamTemp = new PlayerAutoMarchParam();
		playerAutoMarchParamTemp.unSerialize(this.autoMarchParam);
		playerAutoMarchParamTemp.setEntity(this);
		this.playerAutoMarchParam = playerAutoMarchParamTemp;
	}

	@Override
	public void beforeWrite() {		
		this.dressItemInfo = SerializeHelper.mapToString(dressItemInfoMap);
		//自动行军参数序列化
		if(Objects.nonNull(this.playerAutoMarchParam)){
			this.autoMarchParam = this.playerAutoMarchParam.serialize();
		}
	}

	public void addDressItemInfo(Integer key, Integer value) {
		this.dressItemInfoMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeDressItemInfo(Integer key) {
		this.dressItemInfoMap.remove(key);
		this.notifyUpdate();
	}
	
	public boolean containDressItem(int dressId) {
		return this.dressItemInfoMap.containsKey(dressId);
	}



	
	public PlayerAutoMarchParam getPlayerAutoMarchParam() {
		return playerAutoMarchParam;
	}
	
	
	public int getAutoGuildCityMoveCnt() {
		return autoGuildCityMoveCnt;
	}
	
	public void setAutoGuildCityMoveCnt(int autoGuildCityMoveCnt) {
		this.autoGuildCityMoveCnt = autoGuildCityMoveCnt;
	}


	@Override
	public String getPrimaryKey() {
		return this.playerId;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public String getOwnerKey() {
		return playerId;
	}
}