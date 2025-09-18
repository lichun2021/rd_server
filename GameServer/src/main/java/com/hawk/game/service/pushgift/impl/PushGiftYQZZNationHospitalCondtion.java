package com.hawk.game.service.pushgift.impl;

import java.util.List;
import java.util.Objects;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

public class PushGiftYQZZNationHospitalCondtion extends AbstractPushGiftCondition {
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		int groupId = cfg.getGroupId();

		int statistics = playerData.getPushGiftEntity().getStatistics(groupId);
		if (statistics > 0) {
			return false;
		}
		
		IYQZZPlayer player = YQZZRoomManager.getInstance().makesurePlayer(playerData.getPlayerId());
		if(Objects.isNull(player)){
			return false;
		}
		
		int alreadyExist = player.getPushGiftDeadCnt();
		if (cfgParam.get(0).intValue() > alreadyExist) {
			return false;
		}
		player.setPushGiftDeadCnt(0);
		playerData.getPushGiftEntity().addStatistics(groupId, alreadyExist);
		return true;

	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.PUSH_GIFT_3700.getType();
	}

}
