package com.hawk.activity.type.impl.evolution.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 英雄进化之路活动任务成就配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/evoroad/evoroad_achieve.xml")
public class EvolutionTaskCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;
	/** 可完成次数 */
	private final int repeatVal;
	/** 任务类型 */
	private final int taskType;
	/** 条件值 */
	private final String conditionVal;
	/** 每次完成奖励经验 */
	private final int exp;
	/** 活动期间每日重置  */
	private final int time;

	private EvolutionTaskType evolutionTaskType;

	private List<Integer> conditionList;
	
	private int conditionValue;

	public EvolutionTaskCfg() {
		id = 0;
		taskType = 0;
		conditionVal = "";
		repeatVal = 0;
		exp = 0;
		time = 0;
	}

	public int getId() {
		return id;
	}

	public int getRepeatVal() {
		return repeatVal;
	}

	public int getExp() {
		return exp;
	}
	
	public int getTime() {
		return time;
	}

	public EvolutionTaskType getEvolutionTaskType() {
		return evolutionTaskType;
	}

	public List<Integer> getConditionList() {
		return conditionList;
	}

	public int getConditionValue() {
		return conditionValue;
	}
	
	@Override
	protected boolean assemble() {
		evolutionTaskType = EvolutionTaskType.getType(taskType);
		if (evolutionTaskType == null) {
			HawkLog.errPrintln("taskType type not found! type: {}", taskType);
			return false;
		}
		List<Integer> conditionValueList = SerializeHelper.stringToList(Integer.class, conditionVal, SerializeHelper.ATTRIBUTE_SPLIT);
		conditionList = conditionValueList.subList(0, conditionValueList.size() - 1);
		conditionValue = conditionValueList.get(conditionValueList.size() - 1);
		return true;
	}

}
