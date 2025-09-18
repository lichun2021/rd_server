package com.hawk.activity.type.impl.spaceguard.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;
import com.hawk.serialize.string.SerializeHelper;
																	
@HawkConfigManager.XmlResource(file = "activity/space_machine_guard/space_machine_guard_points_get.xml")
public class SpaceGuardPointCfg extends HawkConfigBase {
	@Id
	private final int Id;

	/** 任务类型 */
	private final int taskType;

	/** 条件值 */
	private final String conditionValue;
	
	private final int exp;
	
	private final int expLimit;
	
	private SpaceMechaTaskType pointTaskType;
	
	private List<Integer> conditionList;
	
	private int conditionVal;

	public SpaceGuardPointCfg() {
		Id = 0;
		taskType = 0;
		conditionValue = "";
		exp = 0;
		expLimit = 0;
	}

	@Override
	protected boolean assemble() {
		pointTaskType = SpaceMechaTaskType.getType(taskType);
		if (pointTaskType == null) {
			return false;
		}
		
		List<Integer> conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		conditionList = conditionValueList.subList(0, conditionValueList.size() - 1);
		conditionVal = conditionValueList.get(conditionValueList.size() - 1);
		return true;
	}

	public int getId() {
		return Id;
	}

	public int getType() {
		return taskType;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public int getExp() {
		return exp;
	}

	public int getExpLimit() {
		return expLimit;
	}

	public List<Integer> getConditionList() {
		return conditionList;
	}

	public int getConditionVal() {
		return conditionVal;
	}
	
	public SpaceMechaTaskType getTaskType() {
		return pointTaskType;
	}
	
}
