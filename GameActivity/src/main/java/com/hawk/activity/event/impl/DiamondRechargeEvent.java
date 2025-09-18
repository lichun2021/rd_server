package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 充值钻石
 * @author Jesse
 *
 */
public class DiamondRechargeEvent extends ActivityEvent {
	/** 商品Id*/
	private String goodsId;
	
	/** 充值钻石数量*/
	private int diamondNum;
	
	public DiamondRechargeEvent(){ super(null);}
	public DiamondRechargeEvent(String playerId, String goodsId, int diamondNum) {
		super(playerId, true);
		this.goodsId = goodsId;
		this.diamondNum = diamondNum;
	}
	
	public String getGoodsId() {
		return goodsId;
	}

	public int getDiamondNum() {
		return diamondNum;
	}
	
	@Override
	public boolean isSkip() {
		return true;
	}
}
