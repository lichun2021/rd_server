package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class TravelShopBuyParser implements OrderTaskParser<TravelShopPurchaseEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.TRAVEL_SHOP_BUY;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, TravelShopPurchaseEvent event) {
		//Old
		if (!event.isCommonPool()) {
			return false;
		}
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

}
