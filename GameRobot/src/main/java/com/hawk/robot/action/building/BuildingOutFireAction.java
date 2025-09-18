package com.hawk.robot.action.building;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;

/**
 * 
 * 城墙灭火
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class BuildingOutFireAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.CITYDEF_REQ_C_VALUE));
	}
	
	public static synchronized void outFire(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, BuildingOutFireAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		if(robot.getPuidNum() % 2 == 0) {
			return;
		}
		
		CityDefPB cityDefInfo = robot.getCityData().getCityDefInfo();
		if (cityDefInfo == null || !cityDefInfo.hasOnFireEndTime() || cityDefInfo.getOnFireEndTime() < HawkTime.getMillisecond()) {
			return;
		}
		
		BuildingPB cityWall = robot.getBuildingByType(BuildingType.CITY_WALL_VALUE).get(0);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_OUTFIRE_C_VALUE));
		robot.getCityData().getLastExecuteTime().put(BuildingOutFireAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("out fire action, playerId: {}, city wall status: {}, onfire endtime: {}", robot.getPlayerId(), cityWall.getStatus(), cityDefInfo.hasOnFireEndTime() ? cityDefInfo.getOnFireEndTime() : 0);
		cityDefInfo = cityDefInfo.toBuilder().setOnFireEndTime(HawkTime.getMillisecond()).build();
		robot.getCityData().updateCityDef(cityDefInfo);
	}
	
}
