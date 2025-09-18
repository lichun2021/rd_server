package com.hawk.activity.type.impl.hellfiretwo.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/hellfire_two/activity_hellfire_two_condition.xml")
public class ActivityHellFireTwoConditionCfg extends HawkConfigBase {
	/**
	 * 条件
	 */
	@Id
	private final int conditionType;
	/**
	 * 系数
	 */
	private final String scoreCof;
	/**
	 * 系数
	 */
	private Map<Integer, Integer> scoreCfgMap;
	
	public ActivityHellFireTwoConditionCfg() {
		this.conditionType = 0;
		this.scoreCof = "";
	}
	
	public boolean assemble() {
		String[] scoreCofArray = scoreCof.split(SerializeHelper.BETWEEN_ITEMS);
		scoreCfgMap = new HashMap<>();
		for (String scoreCofItem : scoreCofArray) {
			String[] scoreCofItemArray = scoreCofItem.split(SerializeHelper.ATTRIBUTE_SPLIT);
			if (scoreCofItemArray.length !=2) {
				throw new InvalidParameterException("ActivityHellFireConditionCfg itemLength not equal 2 scoreCofItemError=>"+scoreCof);
			}
			
			Integer oldValue = scoreCfgMap.put(Integer.parseInt(scoreCofItemArray[0]), Integer.parseInt(scoreCofItemArray[1]));
			if (oldValue != null) {
				throw new InvalidParameterException("ActivityHellFireConditionCfg repeated item scoreCofItemError=>"+scoreCof);
			}
		}
		 
		return true; 
	}

	public int getConditionType() {
		return conditionType;
	}

	public Map<Integer, Integer> getScoreCfgMap() {
		return scoreCfgMap;
	}
}
