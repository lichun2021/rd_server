package com.hawk.robot.action.building;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;

/**
 * 城墙修复
 * @author lating
 *
 */
@RobotAction(valid = false)
public class BuildingRepairAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		doBuildingRepair(gameRobotEntity);
	}
	
	public static synchronized void doBuildingRepair(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, BuildingRepairAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		if(robot.getPuidNum() % 2 == 0) {
			return;
		}
		
		BuildingPB cityWall = robot.getBuildingByType(BuildingType.CITY_WALL_VALUE).get(0);
		if (cityWall.getStatus() == BuildingStatus.COMMON) {
			return;
		}
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_REPAIR_C_VALUE));
		robot.getCityData().getLastExecuteTime().put(BuildingRepairAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("repair city wall, playerId: {}", robot.getPlayerId());
	}
}
