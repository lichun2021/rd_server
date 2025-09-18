package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.service.pushgift.PushGiftConditionEnum;

@HawkConfigManager.XmlResource(file = "xml/push_gift_group.xml")
public class PushGiftGroupCfg extends HawkConfigBase {
	/**
	 * 组ID
	 */
	@Id
	protected final int groupId;
	/**
	 * 条件类型
	 */
	protected final int conditionType;
	/**
	 * 分组类型
	 */
	protected final int groupType;
	/**
	 * 是否出售
	 */
	protected final int isSale;
	/**
	 * 出现时间
	 */
	protected final int limitTime;
	/**
	 * 一天内可以触发的次数
	 */
	protected final int timeInterval;
	/**
	 * 终身可以触发的次数
	 */
	protected final int allInterval;
	/**
	 * 特殊.
	 */
	private final boolean special;
	/**
	 * 礼包触发单位时长，单位小时
	 */
	protected final int dataTimeInterval;
	
	private static Map<Integer, Integer> groupDateTimeIntervalMap = new HashMap<Integer, Integer>();
	/**
	 * 使用道具类型的推送礼包groupId
	 */
	private static Map<Integer, List<Integer>> conditionTypeGroupIdMap = new HashMap<Integer, List<Integer>>();  
		
	public PushGiftGroupCfg() {
		groupId = 0;
		conditionType = 0;
		groupType = 0;
		isSale = 0;
		limitTime = 0;
		timeInterval = 0;
		special = false;
		dataTimeInterval = 0;
		allInterval = 0;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getConditionType() {
		return conditionType;
	}

	public int getGroupType() {
		return groupType;
	}

	public int getIsSale() {
		return isSale;
	}

	public int getLimitTime() {
		return limitTime;
	}

	public int getTimeInterval() {
		return timeInterval;
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public int getDataTimeInterval() {
		return dataTimeInterval;
	}
	
	public int getAllInterval() {
		return allInterval;
	}

	public boolean assemble() {
		if (dataTimeInterval > 0) {
			groupDateTimeIntervalMap.put(groupId, dataTimeInterval);
		}
		
		List<Integer> groupIdList = conditionTypeGroupIdMap.get(conditionType);
		if (groupIdList == null) {
			groupIdList = new ArrayList<Integer>();
			conditionTypeGroupIdMap.put(conditionType, groupIdList);
		}
		groupIdList.add(groupId);
		
		return true;
	}
	
	public static Map<Integer, Integer> getGroupDateTimeIntervalMap() {
		return groupDateTimeIntervalMap;
	}

	public static List<Integer> getGroupIdsByConditionType(PushGiftConditionEnum conditionType) {
		return conditionTypeGroupIdMap.getOrDefault(conditionType.getType(), Collections.emptyList());
	}
	
}
