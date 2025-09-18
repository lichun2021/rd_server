package com.hawk.activity.type.impl.backFlow.returnArmyExchange.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_return_army_exchange")
public class ReturnArmyExchangeEntity extends HawkDBEntity implements IActivityDataEntity {

	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;

    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;

    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;

	/**
	 * 回流次数
	 */
    @IndexProp(id = 4)
	@Column(name = "backCount", nullable = false)
	private int backCount;

	/** 发展军资换购信息 */
    @IndexProp(id = 5)
	@Column(name = "exchangeInfos", nullable = true)
	private String exchangeInfos;
	
	
	
	/**
	 * 玩家回归奖励类型
	 */
    @IndexProp(id = 6)
	@Column(name = "backType", nullable = false)
	private int backType;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 7)
	@Column(name = "overTime", nullable = false)
	private long overTime;
	
	/**
	 * 当前期开始时间
	 */
    @IndexProp(id = 8)
	@Column(name = "startTime", nullable = false)
	private long startTime;

    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private Map<Integer, Integer> exchangeNumMap = new HashMap<>();

	public ReturnArmyExchangeEntity() {
	}

	public ReturnArmyExchangeEntity(String playerId) {
		this.playerId = playerId;
	}

	public ReturnArmyExchangeEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getBackCount() {
		return backCount;
	}

	public void setBackCount(int backCount) {
		this.backCount = backCount;
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
	public void beforeWrite() {
		exchangeInfos = SerializeHelper.mapToString(exchangeNumMap);
	}

	public String getExchangeInfos() {
		return exchangeInfos;
	}

	public void setExchangeInfos(String exchangeInfos) {
		this.exchangeInfos = exchangeInfos;
	}

	public Map<Integer, Integer> getExchangeNumMap() {
		return exchangeNumMap;
	}

	public void setExchangeNumMap(Map<Integer, Integer> exchangeNumMap) {
		this.exchangeNumMap = exchangeNumMap;
	}
	
	public void resetExchangeNumMap(){
		this.exchangeNumMap = new HashMap<>();
		this.notifyUpdate();
	}

	@Override
	public void afterRead() {
		exchangeNumMap = SerializeHelper.stringToMap(exchangeInfos, Integer.class, Integer.class);
	}

	@Override
	public String getPrimaryKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrimaryKey(String arg0) {
		// TODO Auto-generated method stub

	}

	public int getBackType() {
		return backType;
	}

	public void setBackType(int backType) {
		this.backType = backType;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	

}
