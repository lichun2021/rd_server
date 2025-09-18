package com.hawk.game.service.pushgift.impl;

import java.util.List;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 量子扩充礼包
 * 礼包条件：任一装备量子扩充等级达到XX级且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：XX_vipMin_vipMax
 * @author lating
 *
 */
public class PushGiftCondition3900 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(1) || vipLevel > cfgParam.get(2)) {
			return false;
		}
		
		//装备量子扩充等级判断
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
		return PushGiftConditionEnum.PUSH_GIFT_3900.getType();
	}
}
