package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 指挥官经验礼包
 * 
 * @author lating
 *
 */
public class PushGiftCommanderExpCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式：37_2000000_0_7  指挥官等级_累计增加指挥官经验值_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int playerLevel = playerData.getPlayerBaseEntity().getLevel();
		int playerExp = playerData.getPlayerBaseEntity().getExp();
		// 指挥官等级条件判断
		if (cfgParam.get(0) != playerLevel) {
			return false;
		}
		
		int groupId = cfg.getGroupId();
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 只有首次满足条件时才触发
		if (statistics > cfgParam.get(1)) {
			return false;
		}
		
		// 更新统计值: 如果的减经验值的变动，则不处理
		if (playerExp > statistics) {
			playerData.getPushGiftEntity().addStatistics(groupId, playerExp - statistics);
			statistics = playerExp;
		}
		
		// 累计增加指挥官经验值判断
		if (statistics < cfgParam.get(1)) {
			return false;
		}
		
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(2) || vipLevel > cfgParam.get(3)) {
			playerData.getPushGiftEntity().removeStatistics(groupId);
			return false;
		}
		
		return true;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.COMMANDER_EXP.getType(); 
	}

}
