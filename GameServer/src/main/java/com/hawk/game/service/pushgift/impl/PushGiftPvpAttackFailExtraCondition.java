package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftPvpAttackFailExtraCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return param.get(0).intValue() >= cfgParam.get(2).intValue() && (cfgParam.get(3) <= param.get(1) && param.get(1) <= cfgParam.get(4));
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.ALL_ATTACK_FAIL_EXTRA.getType();
	}

}
