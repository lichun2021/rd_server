package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftOneKeyHeroUpCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}
	
	@Override 
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int level = playerData.getConstructionFactoryLevel();
		if (level < cfgParam.get(0) || level > cfgParam.get(1)) {
			return false;
		}
		Integer itemType = null;
		for (int i = 2; i < cfgParam.size(); i++) {
			itemType = cfgParam.get(i);
			int num = playerData.getItemNumByItemType(itemType);
			if (num > 0) {
				return false;
			}
		}
		
		return true;
	}
	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.ONE_KEY_HERO_UP.getType(); 
	}

}
