package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.ToolSpeedUpType;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 训练加速礼包
 * 
 * @author lating
 *
 */
public class PushGiftTrainSpeedCondition extends AbstractPushGiftCondition {
	
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式： 7200_0_3600_0_7  消耗时间_存量时间起始值_存量时间终止值_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int speedTimeAdd = param.get(0);  // 道具加速时间增量
		int groupId = cfg.getGroupId();
		playerData.getPushGiftEntity().addStatistics(groupId, speedTimeAdd);
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 累计消耗加速道具时间判断
		if (statistics < cfgParam.get(0)) {
			return false;
		}
		
		// 造兵加速道具存量判断
		int speedTime = playerData.getItemSpeedTimeByItemType(ToolSpeedUpType.TOOL_SPEED_SOILDER_VALUE);
		if (speedTime < cfgParam.get(1) || speedTime > cfgParam.get(2)) {
			return false;
		}

		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(3) || vipLevel > cfgParam.get(4)) {
			return false;
		}
		
		// 礼包触发条件达到了，清数据
		playerData.getPushGiftEntity().removeStatistics(groupId);
		
		return true;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.TRAIN_SPEED.getType(); 
	}

}
