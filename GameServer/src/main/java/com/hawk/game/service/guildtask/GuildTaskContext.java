package com.hawk.game.service.guildtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.game.service.guildtask.impl.IGuildTask;

/**
 * 联盟任务任务上下文
 * 
 * @author jesse
 *
 */
public class GuildTaskContext {

	/**
	 * 实例
	 */
	private static GuildTaskContext instance;

	/**
	 * 联盟任务
	 */
	private static Map<GuildTaskType, IGuildTask> tasks;
	
	
	/**
	 * 构造
	 */
	private GuildTaskContext() {
		
	}

	/**
	 * 获取实例
	 * @return
	 */
	public static GuildTaskContext getInstance() {
		if (instance == null) {
			instance = new GuildTaskContext();
		}
		
		return instance;
	}

	/**
	 * 获取任务
	 * @param taskType
	 * @return
	 */
	public IGuildTask getTask(GuildTaskType taskType) {
		return tasks.get(taskType);
	}
	
	
	/**
	 * 初始化
	 */
	public void init() {
		tasks = new HashMap<GuildTaskType, IGuildTask>();
		String packageName = IGuildTask.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, GuildTask.class);
		for (Class<?> cls : classList) {
			try {
				tasks.put(cls.getAnnotation(GuildTask.class).taskType(), (IGuildTask) cls.newInstance());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
