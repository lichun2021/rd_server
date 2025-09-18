package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 装备科技解锁礼包
 * 
 * 礼包类型 = 2200
 * 礼包条件：解锁装备技术研究时触发，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：vipMin_vipMax
 * 
 * @author Golden
 *
 */
public class PushGiftEquipResearchUnlockCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();

		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(0) || vipLevel > cfgParam.get(1)) {
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
		return PushGiftConditionEnum.EQUIP_RESEARCH_UNLOCK.getType(); 
	}

}
