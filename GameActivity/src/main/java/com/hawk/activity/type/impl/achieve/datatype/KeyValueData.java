package com.hawk.activity.type.impl.achieve.datatype;

import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * <pre>
 * KEY-VALUE 映射类型数据
 * 此数据结构中数据库数据存在1个值，用于存储配置key所对应玩家的数值变化，数值位于[0]号位置
 * 配置数据中存在2个值，[0]号位置为key，可理解为目标类型，[1]号位置为value，即为玩家所要达到的数值
 * </pre>
 * @author PhilChen
 *
 */
public class KeyValueData implements AchieveData {

	@Override
	public boolean isGreaterOrEqual(AchieveItem item, AchieveConfig achieveConfig) {
		int value = item.getValue(0);
		int configValue = achieveConfig.getConditionValue(1);
		if (value >= configValue) {
			return true;
		}
		return false;
	}

	@Override
	public int getShowValue(AchieveItem item) {
		return item.getValue(0);
	}

	@Override
	public int getConfigShowValue(AchieveConfig achieveConfig) {
		return achieveConfig.getConditionValue(1);
	}

}
