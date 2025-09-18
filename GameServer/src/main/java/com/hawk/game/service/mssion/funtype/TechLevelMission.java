package com.hawk.game.service.mssion.funtype;

import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.PlayerData;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 科技等级任务
 * 
 * @author lating
 *
 */
public class TechLevelMission implements IFunMission {

	@Override
	public long genMissionNum(PlayerData playerData, int funId) {
		TechnologyEntity techEntity = playerData.getTechEntityByTechId(funId);
		if(techEntity != null) {
			return techEntity.getLevel();
		}
		
		return 0;
	}

	@Override
	public MissionFunType getFunType() {
		return MissionFunType.FUN_TECH_LEVEL;
	}

}
