package com.hawk.activity.type.impl.order.task;

import java.util.Optional;

import org.hawk.log.HawkLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.type.impl.order.IOrderActivity;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;

public interface OrderTaskParser<T extends OrderEvent> {
	static Logger logger = LoggerFactory.getLogger("Server");

	OrderTaskType getTaskType();

	/**
	 * 更新任务数据
	 * 
	 * @param dataEntity
	 * @param orderItem
	 * @param event
	 * @return
	 */
	boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, T event);
	
	
	default boolean onAddValue(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, long addValue) {
		int oldTimes = orderItem.getFinishTimes();
		long oldVal = orderItem.getValue();
		int maxTimes = cfg.getRepeatVal();
		// 达到上限
		if (oldTimes >= maxTimes) {
			return false;
		}
		int conditionVal = cfg.getConditionValue();
		long newVal = oldVal + addValue;
		int addTimes = (int) (newVal / conditionVal);
		long remainVal = newVal % conditionVal;
		if (oldTimes + addTimes >= maxTimes) {
			remainVal = 0;
			addTimes = Math.min(addTimes, maxTimes - oldTimes);
		}
		orderItem.setFinishTimes(oldTimes + addTimes);
		orderItem.setValue(remainVal);
		if (addTimes > 0) {
			int addExp = cfg.getExp() * addTimes;
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getGameActivityByType(dataEntity.getActivityType());
			if (opActivity.isPresent()) {
				ActivityBase activity = opActivity.get();
				if(activity instanceof IOrderActivity){
					IOrderActivity orderActivity = (IOrderActivity) activity;
					orderActivity.addExp(dataEntity, addExp, IOrderActivity.EXP_REASON_TASK, cfg.getId());
					orderActivity.logOrderFinishId(dataEntity, cfg, orderItem, addTimes);
				}
			}
			HawkLog.logPrintln("OrderActivity task finish, oldTimes: {}, newTimes: {}, taskId: {}", oldTimes, orderItem.getFinishTimes(), cfg.getId());
		}  
		return true;
	}

	/**
	 * 该任务是否全部完成
	 * 
	 * @param orderItem
	 * @return
	 */
	default boolean finished(OrderItem orderItem, IOrderTaskCfg cfg) {
		return orderItem.getFinishTimes() >= cfg.getRepeatVal();
	}

}
