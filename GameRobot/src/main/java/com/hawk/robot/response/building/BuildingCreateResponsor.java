package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.tech.TechLevelUpAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = {HP.code.BUILDING_CREATE_PUSH_VALUE})
public class BuildingCreateResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		BuildingPB newBuilding = protocol.parseProtocol(BuildingPB.getDefaultInstance());
		RobotLog.cityDebugPrintln("building create response, playerId: {}, buildingId: {}, protocol: {}", robotEntity.getPlayerId(), newBuilding.getBuildCfgId(), protocol.getType());
		robotEntity.getCityData().refreshBuildingData(newBuilding);
		if(newBuilding.getStatus() == BuildingStatus.COMMON) {
			robotEntity.getCityData().unlockTechByBuilding(newBuilding.getBuildCfgId());
			TechLevelUpAction.techLevelUpAfterBuilding(robotEntity, newBuilding.getBuildCfgId());
		}
	}

}
