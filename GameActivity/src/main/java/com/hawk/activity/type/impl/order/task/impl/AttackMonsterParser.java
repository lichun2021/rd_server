package com.hawk.activity.type.impl.order.task.impl;


import java.util.List;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class AttackMonsterParser implements OrderTaskParser<MonsterAttackEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.KILL_OLD_MONSTER_LEVEL;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, MonsterAttackEvent event) {
		int lvl = event.getMonsterLevel();
		List<Integer> conditionList = cfg.getConditionList();
		if (conditionList.size() != 2 || (lvl < conditionList.get(0) || lvl > conditionList.get(1))) {
			return false;
		}

		if (event.isKill()) {
			return onAddValue(dataEntity, cfg, orderItem, event.getAtkTimes());
		}
		return false;
	}

	

}
