package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 资源产出速率任务
 * 
 * @author lating
 *
 */
public class ResourceRateMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return playerData.getResourceOutputRate(funId);
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_RESOURCE_RATE;
	}

}
