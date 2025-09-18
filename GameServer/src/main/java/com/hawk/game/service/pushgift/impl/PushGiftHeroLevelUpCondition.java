package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftHeroLevelUpCondition extends AbstractPushGiftCondition{

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		int heroCfgId = cfgParam.get(0);
		if (heroCfgId == 0) {
			return param.get(1).intValue() == cfgParam.get(1).intValue();
		} else {
			return param.get(0).intValue() == cfgParam.get(0).intValue() && param.get(1).intValue() == cfgParam.get(1).intValue(); 
		}
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.HERO_LEVEL_UP.getType();
	}

}
