package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;

import com.hawk.game.GsConfig;

/**
 * 充值成功信息表
 *
 * @author hawk
 */
@Entity
@Table(name = "recharge_daily")
public class RechargeDailyEntity extends HawkDBEntity {
	// 充值订单号
	@Id
	@Column(name = "billno", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String billno = null;

	// 充值类型：购买钻石礼包或道具直购
	@Column(name = "type")
    @IndexProp(id = 2)
	private int type = 0;
	
	// 充值成功获得的钻石数
	@Column(name = "diamonds")
    @IndexProp(id = 3)
	private int diamonds = 0;

	// 充值的时间
	@Column(name = "time")
    @IndexProp(id = 4)
	private long time = 0;

	// 充值订单token
	@Column(name = "token")
    @IndexProp(id = 5)
	private String token = null;

	// 服务器编号
	@Column(name = "serverId")
    @IndexProp(id = 6)
	private String serverId = null;

	// 玩家id
	@Column(name = "playerId")
    @IndexProp(id = 7)
	private String playerId = null;

	// 玩家puid
	@Column(name = "puid")
    @IndexProp(id = 8)
	private String puid = null;

	// 商品id
	@Column(name = "goodsId")
    @IndexProp(id = 9)
	private String goodsId;

	// 商品价格
	@Column(name = "goodsPrice")
    @IndexProp(id = 10)
	private int goodsPrice = 0;

	// 购买商品支付的货币数
	@Column(name = "payMoney")
    @IndexProp(id = 11)
	private int payMoney = 0;

	// 币种
	@Column(name = "currency")
    @IndexProp(id = 12)
	private String currency = null;
	
	// 购买礼包获得的附带奖励物品
	@Column(name = "awardItems", nullable = true)
    @IndexProp(id = 13)
	private String awardItems = null;

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 14)
	private long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 15)
	private long updateTime;

	@Column(name = "invalid")
    @IndexProp(id = 16)
	private boolean invalid;

	public RechargeDailyEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	/**
	 * 转服后需要判断platId，因为要查redis，为性能考虑不能在此处直接判断，需要在实际调用的地方做判断
	 * @return
	 */
	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public int getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(int goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public int getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(int payMoney) {
		this.payMoney = payMoney;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getAwardItems() {
		return awardItems;
	}

	public void setAwardItems(String awardItems) {
		this.awardItems = awardItems;
	}

	public String getBillno() {
		return billno;
	}

	public void setBillno(String billno) {
		this.billno = billno;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public int getDiamonds() {
		return diamonds;
	}

	public void setDiamonds(int diamonds) {
		this.diamonds = diamonds;
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
		return billno;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		billno = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
	
	public RechargeEntity toRechargeEntity() {
		RechargeEntity entity = new RechargeEntity();
		entity.setBillno(billno);
		entity.setToken(token);
		entity.setServerId(serverId);
		entity.setPlayerId(playerId);
		entity.setPuid(puid);
		entity.setType(type);
		entity.setTime(time);
		entity.setGoodsId(goodsId);
		entity.setCurrency(currency);
		entity.setPayMoney(payMoney);
		entity.setGoodsPrice(goodsPrice);
		entity.setDiamonds(diamonds);
		return entity;
	}
}
