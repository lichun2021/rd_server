package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

/**
 * GM充值记录
 *
 * @author Nannan.Gao
 * @date 2016-10-13 16:44:58
 */
@Entity
@Table(name = "gm_recharge")
public class GmRechargeEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "gmUser", nullable = false)
    @IndexProp(id = 2)
	private String gmUser = "";

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 3)
	private String playerId = "";

	@Column(name = "goodsId", nullable = false)
    @IndexProp(id = 4)
	private String goodsId = "";

	@Column(name = "rechargeGold", nullable = false)
    @IndexProp(id = 5)
	private int rechargeGold;

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

	public GmRechargeEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGmUser() {
		return gmUser;
	}

	public void setGmUser(String gmUser) {
		this.gmUser = gmUser;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public int getRechargeGold() {
		return rechargeGold;
	}

	public void setRechargeGold(int rechargeGold) {
		this.rechargeGold = rechargeGold;
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
