package com.hawk.activity.type.impl.order.task.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class BeatBattleCrossParser implements OrderTaskParser<PvpBattleEvent> {
	
	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.BEAT_BATTLE_CROSS;
	}
	
	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, PvpBattleEvent event) {
		if (event.isSameServer()) {
			return false;
		}
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		Map<Integer, Integer> armyHurtMap = event.getArmyHurtMap();
		Map<Integer, Float> powerMap = ActivityManager.getInstance().getDataGeter().getArmyPowerMap();
		double powerVal = 0;
		for (Entry<Integer, Integer> entry : armyHurtMap.entrySet()) {
			Float basePower = powerMap.get(entry.getKey());
			if (basePower == null) {
				continue;
			}
			powerVal += 1d * basePower * entry.getValue();
		}
		for (Entry<Integer, Integer> entry : armyKillMap.entrySet()) {
			Float basePower = powerMap.get(entry.getKey());
			if (basePower == null) {
				continue;
			}
			powerVal += 1d * basePower * entry.getValue();
		}
		return onAddValue(dataEntity, cfg, orderItem, (long) powerVal);
	}
}
