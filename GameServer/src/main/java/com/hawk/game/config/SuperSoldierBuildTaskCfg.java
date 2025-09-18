package com.hawk.game.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲建筑任务配置数据
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/supersoldier_build_task.xml")
public class SuperSoldierBuildTaskCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int taskId;
	
	protected final String taskParam;
	
	protected final int unlockDramaLevel;
	
	private static Map<Integer, Set<Integer>> taskMap = new HashMap<>();

	public SuperSoldierBuildTaskCfg() {
		id = 0;
		taskId = 0;
		taskParam = "";
		unlockDramaLevel = 0;
	}

	@Override
	protected boolean assemble() {
		Set<Integer> set = taskMap.get(taskId);
		if (set == null) {
			set = new HashSet<Integer>();
			taskMap.put(taskId, set);
		}
		set.add(id);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getTaskId() {
		return taskId;
	}

	public String getTaskParam() {
		return taskParam;
	}
	
	public int getUnlockDramaLevel() {
		return unlockDramaLevel;
	}
	
    public static Set<Integer> getUnitByTaskId(int taskId) {
    	return taskMap.getOrDefault(taskId, Collections.emptySet());
    }

}
