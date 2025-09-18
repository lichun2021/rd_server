package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 指挥官战力任务
 * 
 * @author lating
 *
 */
public class PlayerPowerMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return playerData.getPlayerEntity().getBattlePoint();
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_PLAYER_BATTLE_POINT;
	}

}
