package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.BuyItemConsumeEvent;
import com.hawk.activity.event.impl.HeroUnlockEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class DailyConsumeGiftBuyTimesParser implements OrderTaskParser<BuyItemConsumeEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.DAILY_CONSUME_GIFT_BUY_TIMES;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, BuyItemConsumeEvent event) {
		int addValue = 0;
		final String android210001 = "210001";
		final String android210003 = "210003";
		final String android210006 = "210006";
		final String android210012 = "210012";
		final String android210030 = "210030";
		final String android210045 = "210045";
		
		final String ios220001 = "220001";
		final String ios220003 = "220003";
		final String ios220006 = "220006";
		final String ios220012 = "220012";
		final String ios220030 = "220030";
		final String ios220045 = "220045";
		
		String giftId = event.getGiftId();
		if(giftId.equals(android210001) || giftId.equals(android210003) ||
				giftId.equals(android210006) || giftId.equals(android210012) ||  giftId.equals(android210030) ||
				giftId.equals(ios220001) || giftId.equals(ios220003) ||
				giftId.equals(ios220006) || giftId.equals(ios220012) ||  giftId.equals(ios220030)){
			addValue = 1;
		}
		if(giftId.equals(android210045) || giftId.equals(ios220045) ){
			addValue = 5;
		}
		if(addValue == 0){
			return false;
		}
		return onAddValue(dataEntity, cfg, orderItem, addValue);
	}

}
