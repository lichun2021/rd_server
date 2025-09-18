package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.PushBuildingStatus;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.BUILDING_STATUS_CHANGE_PUSH_VALUE)
public class BuildingStatusChangeResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PushBuildingStatus buildingStatus = protocol.parseProtocol(PushBuildingStatus.getDefaultInstance());
		robotEntity.getCityData().refreshBuildingStatus(robotEntity, buildingStatus);
	}

}
