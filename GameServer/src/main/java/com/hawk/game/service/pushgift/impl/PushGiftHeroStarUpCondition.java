package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftHeroStarUpCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		int heroId = cfgParam.get(0);
		if (heroId == 0) {
			return param.get(1).intValue() == cfgParam.get(1);
		} else {
			return param.get(0).intValue() == cfgParam.get(0).intValue() && param.get(1).intValue() == cfgParam.get(1).intValue(); 
		}
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.HERO_STAR_UP.getType();
	}

}
