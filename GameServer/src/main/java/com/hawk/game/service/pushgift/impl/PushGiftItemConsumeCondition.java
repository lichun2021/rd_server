package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 消耗指定道具礼包
 * 
 * @author lating
 *
 */
public class PushGiftItemConsumeCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式：1300017_100_0_100_0_7  道具ID_消耗道具个数_存量道具个数起始值_存量道具个数终止值_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int itemId = param.get(0);  // 消耗的道具ID
		int itemCount = param.get(1); // 消耗的道具数量
		// 非指定道具不处理
		if (itemId != cfgParam.get(0)) {
			return false;
		}
		
		int groupId = cfg.getGroupId();
		playerData.getPushGiftEntity().addStatistics(groupId, itemCount);
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 累计消耗道具数量判断
		if (statistics < cfgParam.get(1)) {
			return false;
		}
		
		// 道具存量判断
		int remainCount = playerData.getItemNumByItemId(cfgParam.get(0));
		if (remainCount < cfgParam.get(2) || remainCount > cfgParam.get(3)) {
			return false;
		}
		
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(4) || vipLevel > cfgParam.get(5)) {
			return false;
		}
		
		// 礼包触发条件达到了，清数据
		playerData.getPushGiftEntity().removeStatistics(groupId);
		
		return true;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.ITEM_CONSUME.getType(); 
	}

}
