package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class BuildLvlCntParser implements OrderTaskParser<BuildingLevelUpEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.BUILD_LVL_UP;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, BuildingLevelUpEvent event) {
		if(event.isLogin() || event.getIsSpread()){
			return false;
		}
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}
