package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 购买月卡事件
 * 
 * @author golden
 *
 */
public class BuyMonthCardEvent extends ActivityEvent implements OrderEvent {

	/**
	 * 月卡id
	 */
	private int cardId;

	private int consumeMoney;
	
	private int ready;

	public BuyMonthCardEvent(){ super(null);}
	public BuyMonthCardEvent(String playerId, int cardId, int consumeMoney) {
		super(playerId, true);
		this.cardId = cardId;
		this.consumeMoney = consumeMoney;
	}

	public int getCardId() {
		return cardId;
	}

	public int getConsumeMoney() {
		return consumeMoney;
	}

	public int getReady() {
		return ready;
	}

	public void setReady(int ready) {
		this.ready = ready;
	}
}
