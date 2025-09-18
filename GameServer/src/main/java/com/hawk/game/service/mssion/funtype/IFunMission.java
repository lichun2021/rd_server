package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

public interface IFunMission {

	public long genMissionNum(PlayerData playerData, int funId);
	
	public MissionFunType getFunType();
}
