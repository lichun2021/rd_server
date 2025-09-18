package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 建筑等级任务
 * 
 * @author lating
 *
 */
public class BuildLevelMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return playerData.getBuildingMaxLevel(funId);
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_BUILD_LEVEL;
	}

}
