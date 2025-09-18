package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 装备强化礼包
 * 
 * @author lating
 *
 */
public class PushGiftEquipEnhanceCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式：800001_3_0_7  装备强化道具ID_累计消耗加速道具时间_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int enhanceTimesAdd = param.get(0);  // 强化次数增量
		int nextEnhanceNeedItemNum = param.get(1);  // 下一次强化需要的强化道具数量
		
		int groupId = cfg.getGroupId();
		playerData.getPushGiftEntity().addStatistics(groupId, enhanceTimesAdd);
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 累计消耗加速道具时间判断
		if (statistics < cfgParam.get(1)) {
			return false;
		}
		
		// 道具存量判断
		int itemNum = playerData.getItemNumByItemId(cfgParam.get(0));
		if (itemNum >= nextEnhanceNeedItemNum) {
			return false;
		}

		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(2) || vipLevel > cfgParam.get(3)) {
			return false;
		}
		
		// 礼包触发条件达到了，清数据
		playerData.getPushGiftEntity().removeStatistics(groupId);
		
		return true;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.EQUIP_ENHANCE.getType(); 
	}

}
