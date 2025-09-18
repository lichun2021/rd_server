package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 装备研究升级礼包
 * 
 * @author lating
 *
 */
public class PushGiftEquipResearchCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式：1800000_3_0_7  装备研究升级道具ID_累计消耗加速道具时间_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int speedTimeAdd = param.get(0);  // 道具加速时间增量
		int itemId = param.get(1);  // 升级消耗的道具ID
		int nextNeedItemNum = param.get(2);  // 下一次研究升级需要的道具数量
		// 研究类型判断
		if (itemId != cfgParam.get(0)) {
			return false;
		}
		
		int groupId = cfg.getGroupId();
		playerData.getPushGiftEntity().addStatistics(groupId, speedTimeAdd);
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 累计消耗加速道具时间判断
		if (statistics < cfgParam.get(1)) {
			return false;
		}
		
		// 道具存量判断
		int itemNum = playerData.getItemNumByItemId(cfgParam.get(0));
		if (itemNum >= nextNeedItemNum) {
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
		return PushGiftConditionEnum.EQUIP_RESEARCH.getType(); 
	}

}
