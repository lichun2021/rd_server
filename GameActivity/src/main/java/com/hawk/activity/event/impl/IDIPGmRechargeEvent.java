package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 管家渠道充值事件
 */
public class IDIPGmRechargeEvent extends ActivityEvent {

	//对应的钻石数
	private int diamondNum;
	public IDIPGmRechargeEvent(){ super(null);}
	public IDIPGmRechargeEvent(String playerId, int diamondNum) {
		super(playerId, true);
		this.diamondNum = diamondNum;
	}

	public int getDiamondNum() {
		return diamondNum;
	}

	public void setDiamondNum(int diamondNum) {
		this.diamondNum = diamondNum;
	}

}
