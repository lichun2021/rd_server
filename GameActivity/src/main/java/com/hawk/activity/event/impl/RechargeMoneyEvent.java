package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;

/***
 * 充值金额事件
 * 
 * @author yang.rao
 *
 */
public class RechargeMoneyEvent extends ActivityEvent implements EvolutionEvent {

	private int money;
	
	public RechargeMoneyEvent(){ super(null);}
	public RechargeMoneyEvent(String playerId, int moneySum) {
		super(playerId, true);
		this.money = moneySum;
	}

	public int getMoney() {
		return money;
	}
	
	@Override
	public boolean isSkip() {
		return true;
	}
}
