package com.hawk.robot.action.building;

import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.CollectRecruits;
import com.hawk.game.protocol.HP;

/**
 * 
 * 收取资源
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class ResourceCollectAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		BuildingPB building = gameRobotEntity.getCityData().getResBuilding();
		if(building != null) {
			CollectRecruits.Builder builder = CollectRecruits.newBuilder();
			builder.addId(building.getId());
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.COLLECT_RESOURCE_C_VALUE, builder));
			RobotLog.cityDebugPrintln("resource collect action, playerId: {}, buildingId: {}", gameRobotEntity.getPlayerId(), building.getBuildCfgId());
		}
	}
	
	/**
	 * 收取资源
	 * @param robot
	 * @param buildType
	 */
	public static synchronized void collectResource(GameRobotEntity robot, int buildType) {
		if (!ClientUtil.isExecuteAllowed(robot, ResourceCollectAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		List<BuildingPB> buildingList = robot.getBuildingByType(buildType);
		CollectRecruits.Builder builder = CollectRecruits.newBuilder();
		for(BuildingPB building : buildingList) {
			builder.addId(building.getId());
			robot.getCityData().refreshResBuildingCollectTime(building.getId());
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.COLLECT_RESOURCE_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(ResourceCollectAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("collect resource, playerId: {}, buildType: {}", robot.getPlayerId(), buildType);
	}
	
}
