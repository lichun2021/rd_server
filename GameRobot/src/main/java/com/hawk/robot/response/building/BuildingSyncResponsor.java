package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;

import java.util.stream.Collectors;

import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.HPBuildingInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_BUILDING_SYNC_S_VALUE)
public class BuildingSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPBuildingInfoSync buildingInfo = protocol.parseProtocol(HPBuildingInfoSync.getDefaultInstance());
		robotEntity.getCityData().refreshBuildingData(buildingInfo.getBuildingsList().toArray(new BuildingPB[0]));
		robotEntity.getCityData().unlockTechByBuilding(buildingInfo.getBuildingsList().stream()
				.map(e -> e.getBuildCfgId()).collect(Collectors.toList()).toArray(new Integer[0]));
	}

}
