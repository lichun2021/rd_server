package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftBuildingCondition extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		int buildCfgId = cfgParam.get(0);
		if (buildCfgId == 0) {
			if (cfgParam.get(1).intValue() == 0) {
				return true;
			} else {
				return cfgParam.get(1).intValue() == param.get(1).intValue();
			}
		} else {
			if (cfgParam.get(0).intValue() == param.get(0).intValue()) {
				if (cfgParam.get(1) == 0) {
					return true;
				} else {
					return cfgParam.get(1).intValue() == param.get(1).intValue(); 
				}
			} else {
				return false;
			} 
		}
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.BUILDING_PAKCAGE.getType(); 
	}

}
