package com.hawk.game.service.mssion.funtype;

import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 科技研究次数任务
 * 
 * @author lating
 *
 */
public class TechResearchNumMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		return playerData.getTechnologyEntities().stream().mapToInt(e -> e.getLevel()).sum();
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_TECH_RESEARCH;
	}

}
