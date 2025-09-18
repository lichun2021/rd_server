package com.hawk.activity.type.impl.achieve.datatype;

import java.util.List;

import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * <pre>
 * 列表-数值 类型数据
 * 此数据结构中数据库数据存在1个值，用于存储玩家的数值变化，数值位于[0]号位置
 * 配置数据中存在1个数字列表和最后面的1个值，[0]号位置到[n-1]号位置为数字列表，[n]号位置为value，即为玩家所要达到的数值
 * </pre>
 * @author PhilChen
 *
 */
public class ListValueData implements AchieveData {

	@Override
	public boolean isGreaterOrEqual(AchieveItem item, AchieveConfig achieveConfig) {
		int value = item.getValue(0);
		int configValue = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
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
		return achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
	}

	public boolean isInList(List<Integer> list, int value) {
		for (int i = 0; i < list.size() - 1; i++) {
			Integer listValue = list.get(i);
			if (listValue == value) {
				return true;
			}
		}
		return false;
	}
}
