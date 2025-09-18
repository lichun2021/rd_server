package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 直购礼包购买事件
 */
public class PayGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	private int rmb;
	//对应的钻石数
	private int diamondNum;
	
	public PayGiftBuyEvent(){ super(null);}
	public PayGiftBuyEvent(String playerId, String giftId,int rmb,int diamondNum) {
		super(playerId, true);
		this.giftId = giftId;
		this.diamondNum = diamondNum;
		this.rmb = rmb;
	}

	public String getGiftId() {
		return giftId;
	}

	public int getDiamondNum() {
		return diamondNum;
	}

	public void setDiamondNum(int diamondNum) {
		this.diamondNum = diamondNum;
	}
	
	public int getRmb() {
		return rmb;
	}
	
	public void setRmb(int rmb) {
		this.rmb = rmb;
	}
	@Override
	public boolean isSkip() {
		return true;
	}

	

}
