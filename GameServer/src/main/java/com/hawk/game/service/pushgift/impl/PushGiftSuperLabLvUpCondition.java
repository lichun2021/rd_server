package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 超能实验室升级（部件）礼包
 * 
 * @author lating
 *
 */
public class PushGiftSuperLabLvUpCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	/**
	 * cfgParam参数格式：1480001_3_0_7  1级能量源数量_累计升级次数_vip起始值_vip终止值
	 */
	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int lvupTimesAdd = param.get(0);   // 升级次数增量
		int nextLvupNeedItemNum = param.get(1);  // 下次升级需要能量源数量
		
		int groupId = cfg.getGroupId();
		playerData.getPushGiftEntity().addStatistics(groupId, lvupTimesAdd);
		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		// 累计消耗加速道具时间判断
		if (statistics < cfgParam.get(1)) {
			return false;
		}
		
		// 道具存量判断
		int itemNum = playerData.getItemNumByItemId(cfgParam.get(0));
		if (itemNum >= nextLvupNeedItemNum) {
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
		return PushGiftConditionEnum.SUPER_LAB_LEVEL_UP.getType(); 
	}

}
