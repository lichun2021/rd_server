package com.hawk.game.service.guildtask.impl;

import com.hawk.game.protocol.GuildTask.TaskStatus;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;

public interface IGuildTask {

	/**
	 * 刷新任务
	 * 
	 * @param eneity
	 *            实体
	 * @param cfg
	 *            配置
	 */
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg);

	/**
	 * 初始化任务
	 * 
	 * @param taskItem
	 * @param cfg
	 */
	default void initTask(String guildId, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {

	}

	/**
	 * 检测任务完成
	 * 
	 * @param taskItem
	 * @param cfg
	 */
	default boolean checkTaskFinish(GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		if (taskItem.getValue() >= cfg.getValue() && taskItem.getState() == TaskStatus.NOT_REACH_VALUE) {
			taskItem.setState(TaskStatus.NOT_REWARD_VALUE);
			return true;
		}
		return false;
	}
}
