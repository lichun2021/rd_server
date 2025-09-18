package com.hawk.game.entity; 

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import javax.persistence.Transient;
import com.hawk.serialize.string.SerializeHelper;

/**
*	
*	auto generate do not modified
*/
@Entity
@Table(name="player_resource_gift")
public class PlayerResourceGiftEntity extends HawkDBEntity{

	/***/
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId;

	/**null*/
	@Column(name="boughtInfo", nullable = false, length=512)
    @IndexProp(id = 2)
	private String boughtInfo;

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

	/** complex type @boughtInfo*/
	@Transient
	private Map<Integer, Integer> boughtInfoMap;

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getBoughtInfo() {
		return this.boughtInfo; 
	}

	public void setBoughtInfo(String boughtInfo) {
		this.boughtInfo = boughtInfo;
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

	public Map<Integer, Integer> getBoughtInfoMap() {
		return this.boughtInfoMap; 
	}

	public void setBoughtInfoMap(Map<Integer, Integer> boughtInfoMap) {
		this.boughtInfoMap = boughtInfoMap;
	}

	@Override
	public void afterRead() {		
		this.boughtInfoMap = SerializeHelper.stringToMap(boughtInfo, Integer.class, Integer.class);
	}

	@Override
	public void beforeWrite() {		
		this.boughtInfo = SerializeHelper.mapToString(boughtInfoMap);
	}

	public void addBoughtInfo(Integer key, Integer value) {
		this.boughtInfoMap.put(key, value);
		this.notifyUpdate();
	}

	public void removeBoughtInfo(Integer key) {
		this.boughtInfoMap.remove(key);
		this.notifyUpdate();
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("player resource entity primaryKey is playerId");		
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
