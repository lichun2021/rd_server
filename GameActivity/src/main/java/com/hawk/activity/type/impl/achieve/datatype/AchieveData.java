package com.hawk.activity.type.impl.achieve.datatype;

import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 成就数据（配置与玩家数据）
 * @author PhilChen
 *
 */
public interface AchieveData {

	/**
	 * 判断玩家数据是否大于等于配置数据
	 * @param item
	 * @param achieveConfig
	 * @return
	 */
	boolean isGreaterOrEqual(AchieveItem item, AchieveConfig achieveConfig);

	/**
	 * 获取进度显示的玩家数据
	 * @param item
	 * @return
	 */
	int getShowValue(AchieveItem item);

	/**
	 * 获取进度显示的配置数据
	 * @param AchieveConfig
	 * @return
	 */
	int getConfigShowValue(AchieveConfig achieveConfig);
}
