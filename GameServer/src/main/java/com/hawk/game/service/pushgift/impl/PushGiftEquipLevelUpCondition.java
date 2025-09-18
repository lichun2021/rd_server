package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 装备强化到指定等级触发
 * 礼包类型 = 2400
 * 礼包条件：单套装备强化至XX级，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：XX_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftEquipLevelUpCondition extends AbstractPushGiftCondition {
	
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		
		// 装备强化等级判断
		if (cfgParam.get(0) != param.get(0)) {
			return false;
		}
		
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(1) || vipLevel > cfgParam.get(2)) {
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
		return PushGiftConditionEnum.EQUIP_LEVEL.getType(); 
	}
}