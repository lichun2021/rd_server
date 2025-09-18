package com.hawk.activity.type.impl.spaceguard.task;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.speciality.SpaceMechaEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;

public interface SpaceMechaTaskParser<T extends SpaceMechaEvent> {

	SpaceMechaTaskType getTaskType();

	/**
	 * 更新任务数据
	 * 
	 * @param dataEntity
	 * @param orderItem
	 * @param event
	 * @return
	 */
	boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, T event);
	
	
	default boolean onAddValue(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, long addValue) {
		int oldPoints = taskItem.getPoints();
		int maxPoints = cfg.getExpLimit();
		// 达到上限
		if (oldPoints >= maxPoints) {
			return false;
		}
		long oldVal = taskItem.getValue();
		int conditionVal = cfg.getConditionVal();
		long newVal = oldVal + addValue;
		int addPoints = (int) (newVal / conditionVal) * cfg.getExp();
		long remainVal = newVal % conditionVal;
		if (oldPoints + addPoints >= maxPoints) {
			remainVal = 0;
			addPoints = Math.min(addPoints, maxPoints - oldPoints);
		}
		taskItem.setPoints(oldPoints + addPoints);
		taskItem.setValue(remainVal);
		if (addPoints > 0) {
			HawkLog.logPrintln("SpaceMachineGuardActivity task finish, oldPoints: {}, newPoints: {}, taskId: {}", oldPoints, taskItem.getPoints(), cfg.getId());
		}  
		return true;
	}

	/**
	 * 该任务是否全部完成
	 * 
	 * @param taskItem
	 * @return
	 */
	default boolean finished(PointTaskItem taskItem, SpaceGuardPointCfg cfg) {
		return taskItem.getPoints() >= cfg.getExpLimit();
	}

}
