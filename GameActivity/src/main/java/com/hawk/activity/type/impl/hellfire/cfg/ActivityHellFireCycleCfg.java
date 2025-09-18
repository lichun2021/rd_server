package com.hawk.activity.type.impl.hellfire.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;
@HawkConfigManager.XmlResource(file = "activity/hellfire/activity_hellfire_cycle.xml")
public class ActivityHellFireCycleCfg extends HawkConfigBase {
	/**
	 * 周期ID
	 */
	@Id
	private final int cycleId;
	/**
	 * 名字
	 */
	private final String name;
	/**
	 * 阶段条件类型
	 */
	private final String conditionType;
	/**
	 * 积分目标ID
	 */
	private final String targetId;
	/**
	 * 排行榜ID
	 */
	private final int rankId;
	/**
	 * 阶段条件类型
	 */
	private int[] conditionTypeArray;
	/**
	 * 目标ID数组
	 */
	private int[] targetIdArray;
	
	public ActivityHellFireCycleCfg() {
		this.cycleId = 0;
		this.name = "";
		this.conditionType = "";
		this.targetId = "";
		this.rankId = 0;
	}
	
	@Override
	public boolean assemble() {
		conditionTypeArray = SerializeHelper.string2IntArray(conditionType);
		targetIdArray = SerializeHelper.string2IntArray(targetId);
		
		if (conditionTypeArray.length == 0) {
			throw new InvalidParameterException("ActivityHellFireCycleCfg condition length incorrect");
		}
		
		if (targetIdArray.length ==0) {
			throw new InvalidParameterException("ActivityHellFireCycleCfg targetId length incorrect");
		}
		
		return true;
	}

	public int getCycleId() {
		return cycleId;
	}

	public String getName() {
		return name;
	}

	public int getRankId() {
		return rankId;
	}

	public int[] getConditionTypeArray() {
		return conditionTypeArray;
	}

	public int[] getTargetIdArray() {
		return targetIdArray;
	}
}
