package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 装备研究强化到指定等级触发
 * 
 * 礼包类型 = 2300
 * 礼包条件：装备研究强化至XX级，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：xx部件_XX等级_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftEquipResearchLevelCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		
		// 研究部件判断
		if (param.get(0) != cfgParam.get(0)) {
			return false;
		}

		// 研究等级判断
		if (param.get(1) != cfgParam.get(1)) {
			return false;
		}
		
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(2) || vipLevel > cfgParam.get(3)) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.EQUIP_RESEARCH_LEVEL.getType(); 
	}
}