package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 特惠商店购买事件
 * 
 * @author lating
 *
 */
public class TravelShopPurchaseEvent extends ActivityEvent implements OrderEvent {
	/**
	 * 购买消耗的东西：可能是四类普通资源、金币或金条
	 */
	private int costType;
	/**
	 * 消耗数量
	 */
	private int costNum;
	/**
	 * 表示是否在指定的活动开启期间购买
	 */
	private boolean inActivity;
	/**
	 * 表示是否是普通池购买：只有在普通池购买才是true
	 */
	private boolean commonPool;

	public TravelShopPurchaseEvent(){ super(null);}
	public TravelShopPurchaseEvent(String playerId, int costType, int costNum, boolean inActivity, boolean commonPool) {
		super(playerId);
		this.costType = costType;
		this.costNum = costNum;
		this.inActivity = inActivity;
		this.commonPool = commonPool;
	}

	public int getCostType() {
		return costType;
	}
	
	public int getCostNum() {
		return costNum;
	}
	
	public boolean isInActivity() {
		return inActivity;
	}
	
	public boolean isCommonPool() {
		return commonPool;
	}
	
}
