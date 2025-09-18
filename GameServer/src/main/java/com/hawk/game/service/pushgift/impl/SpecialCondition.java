package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class SpecialCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		if (param.isEmpty()) {
			return true;
		}		
		return param.get(0).intValue() == cfgParam.get(0).intValue() && param.get(1).intValue() == cfgParam.get(1).intValue();
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.SPECIAL.getType();
	}

}
