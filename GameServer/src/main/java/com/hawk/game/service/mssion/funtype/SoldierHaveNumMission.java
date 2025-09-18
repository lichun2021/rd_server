package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 士兵拥有数量任务
 * 
 * @author lating
 *
 */
public class SoldierHaveNumMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return GameUtil.getSoldierHaveNum(playerData, funId);
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_TRAIN_SOLDIER_HAVE_NUMBER;
	}

}
