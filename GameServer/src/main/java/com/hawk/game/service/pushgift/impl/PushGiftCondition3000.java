package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 礼包类型 = 3000
 * 礼包条件：第X次将装备泰晶等级强化至Y级，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：X_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftCondition3000 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(2) || vipLevel > cfgParam.get(3)) {
			return false;
		}
		
		int currentTimes = param.get(0);
		if (currentTimes != cfgParam.get(0)) {
			return false;
		}
		
		int currentLevel = param.get(1);
		if (currentLevel != cfgParam.get(1)) {
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
		return PushGiftConditionEnum.PUSH_GIFT_3000.getType();
	}
}
