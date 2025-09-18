package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftUnlockHeroCondtion extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		//判断品质品质是一定会判断的
		if (cfgParam.get(0).intValue() == param.get(0).intValue()) {
			//只有一个的时候说明只考虑品质
			if (cfgParam.size() == 1) {
				return true;
			}
			//quality_heroId_heroId
			if (cfgParam.subList(1, cfgParam.size()).contains(param.get(1))) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.UNLOCK_HERO.getType();
	}

}
