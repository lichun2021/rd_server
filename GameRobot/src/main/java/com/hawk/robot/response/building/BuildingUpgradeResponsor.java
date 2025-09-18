package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.BuildingUpdatePush;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.building.BuildingUpgradeAction;
import com.hawk.robot.action.tech.TechLevelUpAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.BUILDING_CREATE_PUSH_VALUE, HP.code.BUILDING_REBUILD_PUSH_VALUE, HP.code.BUILDING_UPDATE_PUSH_VALUE })
public class BuildingUpgradeResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		BuildingUpdatePush upgradeBuilding = protocol.parseProtocol(BuildingUpdatePush.getDefaultInstance());
		BuildingPB buildingPB = upgradeBuilding.getBuilding();
		robotEntity.getCityData().refreshBuildingData(buildingPB);
		RobotLog.cityPrintln("building upgrade response, playerId: {}, buildingId: {}, protocol: {}", robotEntity.getPlayerId(), buildingPB.getBuildCfgId(), protocol.getType());
		GameRobotApp.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				BuildingUpgradeAction.doBuildingUpgradeAction(robotEntity);
			}
		});
		
		if(buildingPB.getStatus() == BuildingStatus.COMMON) {
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					robotEntity.getCityData().unlockTechByBuilding(buildingPB.getBuildCfgId());
					TechLevelUpAction.techLevelUpAfterBuilding(robotEntity, buildingPB.getBuildCfgId());
				}
			});
		}
	}

}
