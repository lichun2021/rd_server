package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 礼包类型 = 2803
 * 礼包条件：泰能破译达到一定等级，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
    - 等级：plant_soldier_crack_chip     level
 * 配置格式：等级_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftCondition3400 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(1) || vipLevel > cfgParam.get(2)) {
			return false;
		}
		
		int level = param.get(0);
		if (level != cfgParam.get(0)) {
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
		return PushGiftConditionEnum.PUSH_GIFT_3400.getType();
	}
}
