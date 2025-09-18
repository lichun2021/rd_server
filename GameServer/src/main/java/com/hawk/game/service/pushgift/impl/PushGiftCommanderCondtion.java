package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftCommanderCondtion extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		int level = cfgParam.get(0);
		if (level == 0) {
			return true;
		} else {
			return cfgParam.get(0).intValue() == param.get(0).intValue();
		}
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.COMMANDER_PACKAGE.getType();
	}

}
