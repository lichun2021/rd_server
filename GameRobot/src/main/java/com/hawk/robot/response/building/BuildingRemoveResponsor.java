package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingRemovePushPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = {HP.code.BUILDING_REMOVE_PUSH_VALUE})
public class BuildingRemoveResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		BuildingRemovePushPB removePB = protocol.parseProtocol(BuildingRemovePushPB.getDefaultInstance());
		RobotLog.cityDebugPrintln("building remove response, playerId: {}, buildingId: {}", robotEntity.getPlayerId(), removePB.getBuildingUuid());
		robotEntity.getCityData().removeBuilding(removePB.getBuildingUuid());
	}

}
