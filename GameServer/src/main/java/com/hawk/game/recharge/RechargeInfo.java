package com.hawk.game.recharge;

/**
 * 充值买钻信息
 * 
 * @author lating
 *
 */
public class RechargeInfo {
	/**
	 * 商品id
	 */
	private String goodsId;
	/**
	 * 上一次购买时间
	 */
	private long lastRechargeTime;
	/**
	 * 首次购买时间
	 */
	private long firstRechargeTime;
	/**
	 * 购买次数
	 */
	private int count;

	public RechargeInfo() {
	}

	public RechargeInfo(String goodsId, long lastRechargeTime, long firstRechargeTime, int count) {
		this.goodsId = goodsId;
		this.count = count;
		this.firstRechargeTime = firstRechargeTime;
		this.lastRechargeTime = lastRechargeTime;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public long getLastRechargeTime() {
		return lastRechargeTime;
	}

	public void setLastRechargeTime(long lastRechargeTime) {
		this.lastRechargeTime = lastRechargeTime;
	}

	public long getFirstRechargeTime() {
		return firstRechargeTime;
	}

	public void setFirstRechargeTime(long firstRechargeTime) {
		this.firstRechargeTime = firstRechargeTime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
