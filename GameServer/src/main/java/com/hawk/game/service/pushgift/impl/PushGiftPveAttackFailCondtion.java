package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftPveAttackFailCondtion extends AbstractPushGiftCondition {


	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.ALL_ATTACK_FAIL.getType();
	}

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return param.get(0).intValue() >= cfgParam.get(0).intValue();
	}

}
