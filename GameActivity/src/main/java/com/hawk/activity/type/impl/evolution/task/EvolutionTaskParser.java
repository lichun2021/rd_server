package com.hawk.activity.type.impl.evolution.task;

import java.util.Collections;
import java.util.Optional;

import org.hawk.log.HawkLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.evolution.EvolutionActivity;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;

public interface EvolutionTaskParser<T extends EvolutionEvent> {
	static Logger logger = LoggerFactory.getLogger("Server");

	EvolutionTaskType getTaskType();

	/**
	 * 更新任务数据
	 * 
	 * @param dataEntity
	 * @param taskItem
	 * @param event
	 * @return
	 */
	boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, T event);
	
	
	default boolean onAddValue(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, long addValue) {
		int oldTimes = taskItem.getFinishTimes();
		long oldVal = taskItem.getValue();
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
		
		taskItem.setFinishTimes(oldTimes + addTimes);
		taskItem.setValue(remainVal);
		if (addTimes > 0) {
			int addExp = cfg.getExp() * addTimes;
			dataEntity.setExp(dataEntity.getExp() + addExp);
			// 发邮件提示玩家
			Optional<EvolutionActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.EVOLUTION_ACTIVITY.intValue());
			if (opActivity.isPresent()) {
				EvolutionActivity activity = opActivity.get();
				Object[] content = new Object[2];
				content[0] = cfg.getId();
				content[1] = addExp;
				activity.sendMailToPlayer(dataEntity.getPlayerId(), MailId.EVOLUTION_TASK_EXP_REWARD, null, null, content, Collections.emptyList());
				// 积分增加打点记录
				activity.getDataGeter().logEvolutionExpChange(dataEntity.getPlayerId(), addExp, true, cfg.getId());
				// 任务完成记录
				activity.getDataGeter().logEvolutionTask(dataEntity.getPlayerId(), cfg.getId(), taskItem.getFinishTimes());
			}
		}
		
		HawkLog.logPrintln("EvolutionActivity task finish, playerId: {}, oldTimes: {}, newTimes: {}, taskId: {}", dataEntity.getPlayerId(), oldTimes, taskItem.getFinishTimes(), cfg.getId());
		
		return true;
	}

	/**
	 * 该任务是否全部完成
	 * 
	 * @param orderItem
	 * @return
	 */
	default boolean finished(TaskItem taskItem, EvolutionTaskCfg cfg) {
		return taskItem.getFinishTimes() >= cfg.getRepeatVal();
	}

}
