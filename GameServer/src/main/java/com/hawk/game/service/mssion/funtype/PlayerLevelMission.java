package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 指挥官等级任务
 * 
 * @author lating
 *
 */
public class PlayerLevelMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return playerData.getPlayerBaseEntity().getLevel();
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_PLAYER_LEVEL;
	}

}
