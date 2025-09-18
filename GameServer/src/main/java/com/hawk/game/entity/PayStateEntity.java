package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSON;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.recharge.RechargeInfo;

/**
 * 玩家基础数据
 *
 * @author hawk
 *
 */
@Entity
@Table(name = "pay_state")
public class PayStateEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = null;

	// 当前共买了多少钻
	@Column(name = "rechargeGold")
    @IndexProp(id = 2)
	private int rechargeGold = 0;

	// 当前已领取过的奖励Id
	@Column(name = "rechargeAwardId")
    @IndexProp(id = 3)
	private String rechargeAwardId;

	// 最后一次充值时间
	@Column(name = "lastRechargeTime")
    @IndexProp(id = 4)
	private long lastRechargeTime;

	// 所有充值信息, json{goodsId:count}
	@Column(name = "rechargeInfo")
    @IndexProp(id = 5)
	private String rechargeInfo;

	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 6)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 7)
	private long updateTime;

	// 记录是否有效
	@Column(name = "invalid")
    @IndexProp(id = 8)
	private boolean invalid;

	// 每种商品的最近购买时间
	@Transient
	private Map<String, RechargeInfo> rechargeInfoMap = new HashMap<>();

	public PayStateEntity() {
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getRechargeGold() {
		return rechargeGold;
	}

	public void setRechargeGold(int rechargeGold) {
		this.rechargeGold = rechargeGold;
	}

	public String getRechargeAwardId() {
		return rechargeAwardId;
	}

	public void setRechargeAwardId(int rechargeAwardId) {
		this.rechargeAwardId = HawkOSOperator.isEmptyString(this.rechargeAwardId) ? "" + rechargeAwardId : this.rechargeAwardId + "," + rechargeAwardId;
	}

	public long getLastRechargeTime() {
		return lastRechargeTime;
	}

	public void setLastRechargeTime(long lastRechargeTime) {
		this.lastRechargeTime = lastRechargeTime;
	}

	public String getRechargeInfo() {
		return rechargeInfo;
	}

	public void setRechargeInfo(String rechargeInfo) {
		this.rechargeInfo = rechargeInfo;
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

	public Map<String, RechargeInfo> getRechargeInfoMap() {
		return rechargeInfoMap;
	}

	public boolean addRechargeInfo(String goodsId) {
		RechargeInfo rechargeInfo = rechargeInfoMap.get(goodsId);
		long now = HawkTime.getMillisecond();
		if (rechargeInfo == null) {
			rechargeInfo = new RechargeInfo(goodsId, now, now, 1);
			rechargeInfoMap.put(goodsId, rechargeInfo);
		} else {
			rechargeInfo.setCount(rechargeInfo.getCount() + 1);
			rechargeInfo.setLastRechargeTime(now);
		}

		setRechargeInfo(JSON.toJSONString(rechargeInfoMap.values()));
		RedisProxy.getInstance().updateLatestRechargeTime(playerId);
		return true;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("pay state entity primaryKey is playerId");		
	}

	
	public String getOwnerKey() {
		return playerId;
	}
}
